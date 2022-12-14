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
 * ????????????GPT3??????????????????
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
      log.info("[GPT3Handler.getTestMap] ???????????????????????????GPT3??????????????????????????????{}", testSentenceList.size());
      getResultMap(this::writeTestData, this::mapTestData);
      log.info("[GPT3Handler.getTestMap] ??????????????????????????????GPT3??????????????????????????????{}", unLabelSentenceList.size());
      // ?????????????????????
      getResultMap(this::writeGPT3UnlabelData, this::mapUnlabelData);
    } else {
      testSentenceList.addAll(testDataService.getAllByTaskId(taskId));
      batchSize = testSentenceList.size();
      // ??????????????????GPT3????????????
      // ??????????????????
      log.info("[GPT3Handler.getTestMap] ?????????????????????????????????????????????????????????????????????{}", batchSize);
      getResultMap(this::writeTestData, this::mapTestData);
      Long count = unlabelDataService.countUnlabelDataByTaskId(taskId);
      batchSize = count.intValue();
      log.info("[GPT3Handler.getTestMap] ?????????????????????????????????????????????????????????????????????{}", batchSize);
      // ?????????????????????
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
        throw new BusinessIllegalStateException("????????????????????????");
    }*/
  }

  private void getDataListWhenNoRule() {
    // ?????????????????????GPT3?????????????????????????????????????????????????????????????????????????????????????????????????????????
    // 1. ???????????????
    List<TestDataPO> allShowData = testDataService.getAllByTaskId(taskId);
    if (allShowData.size() < batchSize) {
      testSentenceList.addAll(allShowData);
    } else {
      // ??????????????????????????????batchSize???????????????batchSize?????????
      getOneThousandData(allShowData);
    }
    // 2. ????????????????????????
    Long count = unlabelDataService.countUnlabelDataByTaskId(taskId);
    if (count < batchSize) {
      List<UnlabelDataPO> unlabelDataPOList = unlabelDataService.getAllByTaskId(taskId);
      unLabelSentenceList.addAll(unlabelDataPOList);
    } else {
      // ??????????????????????????????batchSize???????????????batchSize?????????
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
    // ???????????????GPT3????????????????????????????????????????????????????????????????????????
    // ???????????????????????????ID
    List<Long> testCacheIds = gptCacheService.getCacheIds(taskId, 1);
    Long showCount = testDataService.countShowTestDataByTaskId(taskId);
    int remain = showCount.intValue() - testCacheIds.size();
    List<TestDataPO> poList = testDataService.selectNoGptCache(taskId, true);
    if (remain < batchSize) {
      // ????????????batchSize?????????????????????
      testSentenceList.addAll(poList);
    } else {
      // ????????????1000?????????????????????1000???
      getOneThousandData(poList);
    }
    // ???????????????????????????ID
    // ????????????GPT?????????ID
    List<Long> unLabelCacheIds = gptCacheService.getCacheIds(taskId, 2);
    // ???set?????????????????????
    Set<Long> longSet = new HashSet<>(unLabelCacheIds);
    Long unlabelCount = unlabelDataService.countUnlabelDataByTaskId(taskId);
    remain = unlabelCount.intValue() - unLabelCacheIds.size();
    if (remain < batchSize) {
      // ????????????batchSize?????????????????????
      List<UnlabelDataPO> dataPOList = unlabelDataService.selectNoGptCache(taskId);
      unLabelSentenceList.addAll(dataPOList);
    } else {
      // ????????????batchSize?????????????????????batchSize???
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
    // ?????????????????????
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
        log.error("[GPT3Handler.getResultMap] ?????????????????????????????????????????????{}", path);
        throw new BusinessIOException("????????????????????????", e);
      }
      // ???????????????oss
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
          log.warn("[GPT3Handler.getResultMap] ????????????????????????{}?????????", file.getAbsolutePath());
        }
      } catch (Exception e) {
        log.error("[GPT3Handler.getResultMap] ?????????????????????????????????", e);
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
      log.info("[GPT3Handler.getResultMap] ?????????????????????????????????????????????{}", dto);
      ResponseEntity<String> response =
          restTemplate.postForEntity(algorithmProperties.getGpt3Url(), dto, String.class);
      log.info("[GPT3Handler.getResultMap] ???????????????????????????????????????{}", response.getBody());
      long start = System.currentTimeMillis();
      // ??????????????????1000???5??????
      long timeout = (long) (batchSize % 1000 > 0 ? batchSize / 1000 + 1 : batchSize / 1000) * 5 * 60 * 1000;
      int size = example.size();
      timeout = timeout * size / 2;
      // ??????30??????
      timeout = Math.min(timeout, TimeUnit.MINUTES.toMillis(30));
      // 2022???5???23???14:30:52 ?????????????????????????????????
      timeout = timeout * 2;
      log.info("[GPT3Handler.getResultMap] ??????????????????{} ???", timeout / 1000);
      while (System.currentTimeMillis() - start < timeout && !checkResult()) {
        // ??????5???????????????????????????????????????
        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
          throw new BusinessIllegalStateException("?????????????????????????????????", e);
        }
      }
      if (checkResult()) {
        // ???????????????????????????????????????????????????
        String s = null;
        File fileDownload = null;
        try {
          String callback = stringRedisTemplate.opsForValue().get(cacheKey);
          if (StringUtils.isBlank(callback)) {
            log.warn("[GPT3Handler.getResultMap] ????????????????????????????????????");
            throw new BusinessIllegalStateException("??????????????????????????????????????????");
          }
          GPTCallbackDTO callbackDTO;
          try {
            callbackDTO = objectMapper.readValue(callback, GPTCallbackDTO.class);
          } catch (JsonProcessingException e) {
            throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
          }
          if (Boolean.FALSE.equals(callbackDTO.getState())) {
            throw new BusinessIllegalStateException(callbackDTO.getDetail());
          }
          s = callbackDTO.getLabels();
          if (StringUtils.isBlank(s)) {
            log.warn("[GPT3Handler.getResultMap] ????????????????????????????????????");
            throw new BusinessIllegalStateException("??????????????????????????????????????????");
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
            throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
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
        // ??????????????????????????????????????????????????????
        throw new BusinessIllegalStateException("????????????????????????");
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
      throw new BusinessIOException("????????????????????????????????????", e);
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
      throw new BusinessIOException("???????????????????????????????????????", e);
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
      throw new BusinessIOException("???????????????????????????????????????", e);
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
    // ??????GPT??????
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
