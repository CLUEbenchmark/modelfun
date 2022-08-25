package com.wl.xc.modelfun.tasks.rule.handlers;

import cn.hutool.core.lang.id.NanoId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.constants.CommonConstant;
import com.wl.xc.modelfun.commons.enums.BuiltinModelType;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.AlgorithmMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.dto.GPTCallbackDTO;
import com.wl.xc.modelfun.entities.dto.GptDTO;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule.ExampleDTO;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.model.SentenceModel;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.GptCacheService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.utils.PageUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 用于调用GPT3模型的处理器
 *
 * @version 1.0
 * @date 2022/5/16 10:16
 */
@Slf4j
public class GPT3Handler extends AbstractOutLabelHandler {

  private AlgorithmProperties algorithmProperties;

  private FileUploadProperties fileUploadProperties;

  private RuleInfoService ruleInfoService;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private GptCacheService gptCacheService;

  private OssService ossService;

  private ObjectMapper objectMapper;

  private StringRedisTemplate stringRedisTemplate;

  private RestTemplate restTemplate;

  private final List<TestDataPO> testSentenceList = new ArrayList<>(1000);

  private final Map<String, Integer> testSentenceMap = new HashMap<>(1000);

  private final List<UnlabelDataPO> unLabelSentenceList = new ArrayList<>(1000);

  private final Map<String, Integer> unLabelSentenceMap = new HashMap<>(1000);
  private final Long ruleId;

  private final String cacheKey;

  private final BuiltinModelRule builtinModelRule;

  private List<String> labels;

  private List<List<String>> example;

  private BuiltinModelType modelType;

  private int batchSize;

  public GPT3Handler(Long taskId, Long ruleId, BuiltinModelRule builtinModelRule) {
    super(taskId);
    this.ruleId = ruleId;
    this.builtinModelRule = builtinModelRule;
    cacheKey = RedisKeyMethods.getGPTCacheKey(taskId, ruleId);
  }

  @Override
  public RuleType getRuleType() {
    return RuleType.GPT3;
  }

  @Override
  public void init() {
    super.init();
    List<Integer> labelIds = builtinModelRule.getLabels();
    Map<Integer, String> map = new HashMap<>(labelMap.size());
    labelMap.forEach((k, v) -> map.put(v, k));
    labels = labelIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    modelType = BuiltinModelType.getFromType(builtinModelRule.getModelName());
    List<ExampleDTO> exampleDTOS = builtinModelRule.getExample();
    example = new ArrayList<>(exampleDTOS.size());
    for (ExampleDTO exampleDTO : exampleDTOS) {
      List<String> list = new ArrayList<>(2);
      list.add(exampleDTO.getSentence());
      list.add(exampleDTO.getLabelDes());
      example.add(list);
    }
    if (modelType == BuiltinModelType.GPT3) {
      batchSize = 1000;
      getDataListWhenNoRule();
      log.info("[GPT3Handler.getTestMap] 使用测试集数据调用GPT3接口，数据量大小为：{}", testSentenceList.size());
      getResultMap(this::writeTestData, this::mapTestData);
      log.info("[GPT3Handler.getTestMap] 使用未标注集数据调用GPT3接口，数据量大小为：{}", unLabelSentenceList.size());
      // 再调用未标注集
      getResultMap(this::writeGPT3UnlabelData, this::mapUnlabelData);
    } else {
      testSentenceList.addAll(testDataService.getAllByTaskId(taskId));
      batchSize = testSentenceList.size();
      // 生成文件调用GPT3算法接口
      // 先调用测试集
      log.info("[GPT3Handler.getTestMap] 使用测试集数据调用内置模型接口，数据量大小为：{}", batchSize);
      getResultMap(this::writeTestData, this::mapTestData);
      Long count = unlabelDataService.countUnlabelDataByTaskId(taskId);
      batchSize = count.intValue();
      log.info("[GPT3Handler.getTestMap] 使用未标注数据调用内置模型接口，数据量大小为：{}", batchSize);
      // 再调用未标注集
      getResultMap(this::writeUnlabelData, this::mapUnlabelData);
    }
    /*switch (modelType) {
      case GPT3:
        batchSize = 10;
        break;
      case SIM:
      case ROBERTA:
        batchSize = algorithmProperties.getGptBatchSize();
        break;
      default:
        throw new BusinessIllegalStateException("不支持的模型类型");
    }*/
  }

  private void getDataListWhenNoRule() {
    // 如果目前还没有GPT3的规则，则分别从测试集和未标注数据集中，各自获取指定条数条数据调用接口
    // 1. 获取测试集
    List<TestDataPO> allShowData = testDataService.getAllByTaskId(taskId);
    if (allShowData.size() < batchSize) {
      testSentenceList.addAll(allShowData);
    } else {
      // 如果测试集数据量大于batchSize，则随机取batchSize条数据
      getOneThousandData(allShowData);
    }
    // 2. 获取未标注数据集
    Long count = unlabelDataService.countUnlabelDataByTaskId(taskId);
    if (count < batchSize) {
      List<UnlabelDataPO> unlabelDataPOList = unlabelDataService.getAllByTaskId(taskId);
      unLabelSentenceList.addAll(unlabelDataPOList);
    } else {
      // 如果未标注数据量大于batchSize，则随机取batchSize条数据
      Random random = new SecureRandom();
      int max = count.intValue();
      Set<Long> randomIndex =
          random
              .longs(0, max)
              .distinct()
              .limit(batchSize)
              .boxed()
              .collect(HashSet::new, HashSet::add, HashSet::addAll);
      List<UnlabelDataPO> poList = unlabelDataService.listByDataId(taskId, randomIndex);
      unLabelSentenceList.addAll(poList);
    }
  }

  private void getDataListWhenHasRule(List<RuleInfoPO> ruleList) {
    // 如果目前有GPT3的规则，则获取新的数据集的时候需要排除已有的语料
    // 获取展示的测试集的ID
    List<Long> testCacheIds = gptCacheService.getCacheIds(taskId, 1);
    Long showCount = testDataService.countShowTestDataByTaskId(taskId);
    int remain = showCount.intValue() - testCacheIds.size();
    List<TestDataPO> poList = testDataService.selectNoGptCache(taskId, true);
    if (remain < batchSize) {
      // 剩余不到batchSize条，那就全取出
      testSentenceList.addAll(poList);
    } else {
      // 剩余大于1000条，那就随机取1000条
      getOneThousandData(poList);
    }
    // 获取未标注数据集的ID
    // 已经经过GPT模型的ID
    List<Long> unLabelCacheIds = gptCacheService.getCacheIds(taskId, 2);
    // 用set存储，方便查询
    Set<Long> longSet = new HashSet<>(unLabelCacheIds);
    Long unlabelCount = unlabelDataService.countUnlabelDataByTaskId(taskId);
    remain = unlabelCount.intValue() - unLabelCacheIds.size();
    if (remain < batchSize) {
      // 剩余不到batchSize条，那就全取出
      List<UnlabelDataPO> dataPOList = unlabelDataService.selectNoGptCache(taskId);
      unLabelSentenceList.addAll(dataPOList);
    } else {
      // 剩余大于batchSize条，那就随机取batchSize条
      Random random = new SecureRandom();
      Set<Long> randomIndex =
          random
              .longs(0, unlabelCount)
              .filter(index -> !longSet.contains(index))
              .distinct()
              .limit(batchSize)
              .boxed()
              .collect(HashSet::new, HashSet::add, HashSet::addAll);
      List<UnlabelDataPO> dataPOList = unlabelDataService.listByDataId(taskId, randomIndex);
      unLabelSentenceList.addAll(dataPOList);
    }
  }

  private void getResultMap(
      Consumer<Writer> writerConsumer, Consumer<List<String>> resultConsumer) {
    // 测试集数据调用
    Path path = Paths.get(fileUploadProperties.getTempPath(), NanoId.randomNanoId() + ".json");
    if (!path.getParent().toFile().exists()) {
      path.getParent().toFile().mkdirs();
    }
    File file = path.toFile();
    file.deleteOnExit();
    String ossUrl;
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        writerConsumer.accept(writer);
      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        log.error("[GPT3Handler.getResultMap] 数据写入文件失败，文件路径为：{}", path);
        throw new BusinessIOException("数据写入文件失败", e);
      }
      // 上传文件到oss
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      Map<String, String> tag = new HashMap<>();
      tag.put(CommonConstant.OSS_TEMP_TAG, "30");
      upload.setDestPath(fileUploadProperties.getOssTempPath() + "/" + file.getName());
      upload.setTagMap(tag);
      ossService.uploadFiles(Collections.singletonList(upload));
      ossUrl = upload.getDestPath();
    } finally {
      try {
        boolean delete = file.delete();
        if (!delete) {
          log.warn("[GPT3Handler.getResultMap] 删除本地临时文件{}失败！", file.getAbsolutePath());
        }
      } catch (Exception e) {
        log.error("[GPT3Handler.getResultMap] 删除本地临时文件失败！", e);
      }
    }
    try {
      GptDTO dto = new GptDTO();
      dto.setLabels(labels);
      dto.setModelName(modelType.getName());
      dto.setRecordId(ruleId);
      dto.setTaskId(taskId);
      dto.setTexts(ossService.getUrlSigned(ossUrl, fileUploadProperties.getExpireTime()));
      String url =
          AlgorithmMethods.generateUrl(
              algorithmProperties.getAlgorithmCallbackUrl(), CallBackAction.BUILTIN_MODEL);
      dto.setCallback(url);
      dto.setExamples(example);
      log.info("[GPT3Handler.getResultMap] 发送内置模型请求，请求参数为：{}", dto);
      ResponseEntity<String> response =
          restTemplate.postForEntity(algorithmProperties.getGpt3Url(), dto, String.class);
      log.info("[GPT3Handler.getResultMap] 调用内置模型接口返回结果：{}", response.getBody());
      long start = System.currentTimeMillis();
      // 超时时间，每1000条5分钟
      long timeout = (long) (batchSize % 1000 > 0 ? batchSize / 1000 + 1 : batchSize / 1000) * 5 * 60 * 1000;
      int size = example.size();
      timeout = timeout * size / 2;
      // 最大30分钟
      timeout = Math.min(timeout, TimeUnit.MINUTES.toMillis(30));
      // 2022年5月23日14:30:52 超时时间改为原先的两倍
      timeout = timeout * 2;
      log.info("[GPT3Handler.getResultMap] 超时时间为：{} 秒", timeout / 1000);
      while (System.currentTimeMillis() - start < timeout && !checkResult()) {
        // 每过5秒检查一次，看看是否有结果
        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
          throw new BusinessIllegalStateException("内置模型任务线程被取消", e);
        }
      }
      if (checkResult()) {
        // 如果有结果，那就从缓存获取文件地址
        String s = null;
        File fileDownload = null;
        try {
          String callback = stringRedisTemplate.opsForValue().get(cacheKey);
          if (StringUtils.isBlank(callback)) {
            log.warn("[GPT3Handler.getResultMap] 接口没有返回任何文件地址");
            throw new BusinessIllegalStateException("服务器内部错误，请联系管理员");
          }
          GPTCallbackDTO callbackDTO;
          try {
            callbackDTO = objectMapper.readValue(callback, GPTCallbackDTO.class);
          } catch (JsonProcessingException e) {
            throw new BusinessIllegalStateException("服务器内部错误，请联系管理员", e);
          }
          if (Boolean.FALSE.equals(callbackDTO.getState())) {
            throw new BusinessIllegalStateException(callbackDTO.getDetail());
          }
          s = callbackDTO.getLabels();
          if (StringUtils.isBlank(s)) {
            log.warn("[GPT3Handler.getResultMap] 接口没有返回任何文件地址");
            throw new BusinessIllegalStateException("服务器内部错误，请联系管理员");
          }
          Path downloadFile = Paths.get(fileUploadProperties.getTempPath(), NanoId.randomNanoId() + ".json");
          fileDownload = downloadFile.toFile();
          fileDownload.deleteOnExit();
          ossService.download(s, fileDownload.getAbsolutePath());
          List<String> result;
          try {
            result = objectMapper.readValue(fileDownload, new TypeReference<>() {
            });
          } catch (IOException e) {
            throw new BusinessIllegalStateException("服务器内部错误，请联系管理员", e);
          }
          resultConsumer.accept(result);
        } finally {
          if (fileDownload != null) {
            fileDownload.delete();
          }
          if (StringUtils.isNotBlank(s)) {
            ossService.deleteFile(s);
          }
        }
      } else {
        // 如果没有结果，那么就是超时，抛出异常
        throw new BusinessIllegalStateException("内置模型任务超时");
      }
    } finally {
      stringRedisTemplate.delete(cacheKey);
      if (ossUrl != null) {
        ossService.deleteFile(ossUrl);
      }
    }
  }

  private boolean checkResult() {
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey));
  }

  private void writeTestData(Writer writer) {
    try {
      for (TestDataPO po : testSentenceList) {
        SentenceModel model = new SentenceModel();
        model.setSentence(po.getSentence());
        writer.write(objectMapper.writeValueAsString(model));
        writer.write("\n");
      }
    } catch (IOException e) {
      throw new BusinessIOException("测试集数据写入文件失败！", e);
    }
  }

  private void mapTestData(List<String> result) {
    for (int i = 0; i < testSentenceList.size(); i++) {
      TestDataPO po = testSentenceList.get(i);
      if (i < result.size()) {
        int label = labelMap.getOrDefault(result.get(i), -1);
        testSentenceMap.put(po.getSentence(), label);
      } else {
        testSentenceMap.put(po.getSentence(), -1);
      }
    }
  }

  private void writeGPT3UnlabelData(Writer writer) {
    try {
      for (UnlabelDataPO po : unLabelSentenceList) {
        SentenceModel model = new SentenceModel();
        model.setSentence(po.getSentence());
        writer.write(objectMapper.writeValueAsString(model));
        writer.write("\n");
      }
    } catch (IOException e) {
      throw new BusinessIOException("未标注集数据写入文件失败！", e);
    }
  }

  private void writeUnlabelData(Writer writer) {
    try {
      Long total = unlabelDataService.countUnlabelDataByTaskId(taskId);
      long totalPage = PageUtil.totalPage(total, 10000L);
      for (long i = 0; i < totalPage; i++) {
        List<UnlabelDataPO> unlabeledDataPOS =
            unlabelDataService.pageByTaskId(taskId, i * 10000L, 10000);
        for (UnlabelDataPO po : unlabeledDataPOS) {
          SentenceModel model = new SentenceModel();
          model.setSentence(po.getSentence());
          writer.write(objectMapper.writeValueAsString(model));
          writer.write("\n");
        }
        unLabelSentenceList.addAll(unlabeledDataPOS);
      }
    } catch (IOException e) {
      throw new BusinessIOException("未标注集数据写入文件失败！", e);
    }
  }

  private void mapUnlabelData(List<String> result) {
    for (int i = 0; i < unLabelSentenceList.size(); i++) {
      UnlabelDataPO po = unLabelSentenceList.get(i);
      if (i < result.size()) {
        int label = labelMap.getOrDefault(result.get(i), -1);
        unLabelSentenceMap.put(po.getSentence(), label);
      } else {
        unLabelSentenceMap.put(po.getSentence(), -1);
      }
    }
  }

  private void getOneThousandData(List<TestDataPO> poList) {
    Random random = new SecureRandom();
    int max = poList.size();
    Set<Integer> randomIndex =
        random
            .ints(0, max)
            .distinct()
            .limit(batchSize)
            .boxed()
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    for (Integer index : randomIndex) {
      testSentenceList.add(poList.get(index));
    }
  }

  @Override
  public int label(String sentence, DatasetType datasetType) {
    if (DatasetType.TEST.equals(datasetType)) {
      return testSentenceMap.getOrDefault(sentence, -1);
    } else if (DatasetType.UNLABELED.equals(datasetType)) {
      return unLabelSentenceMap.getOrDefault(sentence, -1);
    } else {
      return -1;
    }
  }

  @Override
  public void afterLabel() {
    // 更新GPT缓存
    /*List<GptCachePO> testCache =
        testSentenceList.stream()
            .map(
                m -> {
                  GptCachePO cachePO = new GptCachePO();
                  cachePO.setTaskId(taskId);
                  cachePO.setSentenceId(m.getDataId());
                  cachePO.setDataType(1);
                  cachePO.setRuleId(ruleId);
                  return cachePO;
                })
            .collect(Collectors.toList());
    List<GptCachePO> unlabelCache =
        unLabelSentenceList.stream()
            .map(
                m -> {
                  GptCachePO cachePO = new GptCachePO();
                  cachePO.setTaskId(taskId);
                  cachePO.setSentenceId(m.getDataId());
                  cachePO.setDataType(2);
                  cachePO.setRuleId(ruleId);
                  return cachePO;
                })
            .collect(Collectors.toList());
    testCache.addAll(unlabelCache);
    gptCacheService.saveBatch(testCache);*/
  }

  public void setAlgorithmProperties(AlgorithmProperties algorithmProperties) {
    this.algorithmProperties = algorithmProperties;
  }

  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  public void setGptCacheService(GptCacheService gptCacheService) {
    this.gptCacheService = gptCacheService;
  }

  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
}
