package com.wl.xc.modelfun.tasks.algorithm.handlers;

import cn.hutool.core.lang.id.NanoId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.constants.CommonConstant;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.FileMethods;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.model.DatasetInput;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.RuleVotePO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmHandler;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.utils.PageUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022/4/20 18:29
 */
@Slf4j
public abstract class AbstractHandler implements AlgorithmHandler {

  protected RestTemplate restTemplate;

  protected FileUploadProperties fileUploadProperties;

  protected AlgorithmProperties algorithmProperties;

  protected TaskInfoService taskInfoService;

  protected LabelInfoService labelInfoService;

  protected RuleInfoService ruleInfoService;

  protected RuleResultService ruleResultService;

  protected RuleUnlabeledResultService ruleUnlabeledResultService;

  protected TestDataService testDataService;

  protected UnlabelDataService unlabelDataService;

  protected DatasetDetailService datasetDetailService;

  protected ObjectMapper objectMapper;

  protected OssService ossService;

  protected StringRedisTemplate stringRedisTemplate;

  @Override
  public void handle(AlgorithmTask task) {
    try {
      log.info("[AbstractHandler.handle] 开始执行算法任务，任务类型：{}", getType().getName());
      Long taskId = task.getTaskId();
      TaskInfoPO taskInfoPO = taskInfoService.getById(taskId);
      if (taskInfoPO == null) {
        throw new BusinessIllegalStateException("任务id为" + taskId + "的任务不存在");
      }
      long labelCount = labelInfoService.countLabelInfoByTaskId(taskId);
      DatasetInput datasetInput = generateDatasetInput(task);
      datasetInput.setDescription(taskInfoPO.getDescription());
      datasetInput.setDomainType(taskInfoPO.getDomain());
      datasetInput.setKeywords(taskInfoPO.getKeyword());
      datasetInput.setName(taskInfoPO.getName());
      datasetInput.setNumClass(labelCount);
      datasetInput.setTaskType(taskInfoPO.getTaskType().toString());
      // 获取阿里云oss测试集文件地址
      DatasetDetailPO datasetDetailPO = getTestDatasetFile(taskId);
      if (datasetDetailPO == null) {
        throw new BusinessIllegalStateException("任务id为" + taskId + "的测试集文件不存在");
      }
      datasetInput.setTestPath(
          ossService.getUrlSigned(datasetDetailPO.getFileAddress(), 3600 * 1000));
      // 获取阿里云oss未标注数据集文件地址
      datasetInput.setTrainPath(ossService.getUrlSigned(getTrainPath(taskId), 3600 * 1000));
      internalHandle(task, datasetInput);
    } catch (Exception e) {
      handleOnError(task, e);
    }
  }

  protected String getTrainPath(Long taskId) {
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId,
        DatasetType.UNLABELED.getType());
    if (detailPO == null) {
      throw new BusinessIllegalStateException("任务id为" + taskId + "的未标注数据集文件不存在");
    }
    return detailPO.getFileAddress();
  }

  protected DatasetDetailPO getTestDatasetFile(Long taskId) {
    return datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_UN_SHOW.getType());
  }

  protected abstract DatasetInput generateDatasetInput(AlgorithmTask task);

  protected abstract void internalHandle(AlgorithmTask task, DatasetInput datasetInput);

  protected abstract void handleOnError(AlgorithmTask task, Exception exception);

  protected abstract void send(AlgorithmTask task, DatasetInput datasetInput);

  protected Path prepareFile(Long taskId) {
    String localTemp = fileUploadProperties.getTempPath();
    String fileName = NanoId.randomNanoId() + "_" + taskId + ".json";
    return FileMethods.prepareFile(localTemp, fileName);
  }

  protected String getTestDataResult(Long taskId) {
    log.info("[AbstractHandler.getTestDataResult] 开始生成测试集标签矩阵");
    // 总的未显示的测试集语料数量
    Path path = prepareFile(taskId);
    return writeAndUpload(taskId, path, this::writeTestFile);
  }

  private void writeTestFile(Writer writer, Long taskId) {
    try {
      Long total = testDataService.countUnShowTestDataByTaskId(taskId);
      long totalPage = PageUtil.totalPage(total, 10000L);
      for (long i = 0; i < totalPage; i++) {
        List<RuleVotePO> ruleVotePOS =
            ruleResultService.selectTestDataVoteGroup(taskId, i * 10000L, 10000L, false);
        generateVoteResult(writer, ruleVotePOS);
        if (i != totalPage - 1) {
          writer.write(",");
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("生成测试集标签矩阵文件失败", e);
    }
  }

  private void writeUnlabelFile(Writer writer, Long taskId) {
    try {
      Long total = unlabelDataService.countUnlabelDataByTaskId(taskId);
      long totalPage = PageUtil.totalPage(total, 10000L);
      for (long i = 0; i < totalPage; i++) {
        List<RuleVotePO> ruleVotePOS =
            ruleUnlabeledResultService.selectUnlabeledDataVoteGroup(taskId, i * 10000L, 10000L);
        generateVoteResult(writer, ruleVotePOS);
        if (i != totalPage - 1) {
          writer.write(",");
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("生成未标注数据集标签矩阵文件失败", e);
    }
  }

  protected String getUnlabeledDataResult(Long taskId) {
    log.info("[AbstractHandler.getUnlabeledDataResult] 开始生成未标注数据集标签矩阵文件");
    Path path = prepareFile(taskId);
    return writeAndUpload(taskId, path, this::writeUnlabelFile);
  }

  /**
   * 生成一个本地文件并上传oss，该本地文件内容以'['开头，以']'结尾
   *
   * @param taskId   任务id
   * @param path     本地文件路径
   * @param consumer 文件写入操作
   * @return oss文件路径
   */
  protected String writeAndUpload(Long taskId, Path path, BiConsumer<Writer, Long> consumer) {
    File file = path.toFile();
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        writer.write("[");
        consumer.accept(writer, taskId);
        writer.write("]");
      } catch (BusinessException e) {
        throw e;
      } catch (Exception e) {
        throw new BusinessIllegalStateException("生成数据集标签矩阵文件失败！");
      }
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      upload.setDestPath(fileUploadProperties.getOssTempPath() + "/" + file.getName());
      Map<String, String> tag = new HashMap<>();
      tag.put(CommonConstant.OSS_TEMP_TAG, "30");
      upload.setTagMap(tag);
      ossService.uploadFiles(Collections.singletonList(upload));
      return upload.getDestPath();
    } finally {
      try {
        boolean delete = file.delete();
        if (!delete) {
          log.warn("[AbstractHandler.writeAndUpload] 删除本地临时文件{}失败！", file.getAbsolutePath());
        }
      } catch (Exception e) {
        log.error("[AbstractHandler.writeAndUpload] 删除本地临时文件失败！", e);
      }
    }
  }

  protected void generateVoteResult(Writer writer, List<RuleVotePO> ruleVotePOS)
      throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ruleVotePOS.size(); i++) {
      RuleVotePO ruleVotePO = ruleVotePOS.get(i);
      sb.append("[").append(ruleVotePO.getLabelVote()).append("]");
      if (i != ruleVotePOS.size() - 1) {
        sb.append(",");
      }
    }
    writer.write(sb.toString());
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Autowired
  public void setAlgorithmProperties(AlgorithmProperties algorithmProperties) {
    this.algorithmProperties = algorithmProperties;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }
}
