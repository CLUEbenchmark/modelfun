package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.handlers.FileService;
import com.wl.xc.modelfun.tasks.file.handlers.InternalHandle;
import com.wl.xc.modelfun.tasks.file.handlers.LabelSupport;
import com.wl.xc.modelfun.tasks.file.handlers.PreCheckResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 * @version 1.0
 * @date 2022/6/20 9:57
 */
@Slf4j
public class DatasetParseInternal implements InternalHandle {

  private DatasetInfoService datasetInfoService;

  private UnlabelDataService unlabelDataService;

  private TestDataService testDataService;

  private LabelInfoService labelInfoService;

  private RuleResultService ruleResultService;

  private RuleUnlabeledResultService ruleUnlabeledResultService;

  private ObjectMapper objectMapper;

  private FileService fileService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void internalHandle(FileTask fileTask) {
    PreCheckResult result = (PreCheckResult) fileTask.getConfig().get("PreCheckResult");
    // ????????????????????????
    removeOldData(fileTask.getTaskId());
    // ????????????????????????????????????
    DatasetInfoPO po = new DatasetInfoPO();
    po.setName(fileTask.getFileName());
    po.setDatasetAddress(fileTask.getPath());
    po.setCreatePoeple(fileTask.getCreatePeople());
    po.setTaskId(fileTask.getTaskId());
    datasetInfoService.save(po);
    // ???????????????
    saveLabelInfo(fileTask, po);
    // ??????????????????
    saveUnlabelData(fileTask, result, po);
    // ???????????????
    saveTestData(fileTask, result, po);
    // ???????????????
    saveTrainData(fileTask, result, po);
    // ???????????????
    saveValidateData(fileTask, result, po);
    // ??????????????????????????????????????????oss
    List<File> fileList =
        List.of(
            result.getUnlabelFile(),
            result.getTrainFile(),
            result.getTestFile(),
            result.getValFile());
    fileService.uploadDatasetFiles(fileTask, fileList, po.getId(), null, null);
  }

  private void saveValidateData(FileTask fileTask, PreCheckResult result, DatasetInfoPO po) {
    File valFile = result.getValFile();
    int size = (int) (result.getValFileSize() > 10000 ? 10000 : result.getValFileSize());
    saveLabeledData(DatasetType.TEST_SHOW, size, valFile, fileTask.getTaskId(), po.getId());
  }

  private void saveTrainData(FileTask fileTask, PreCheckResult result, DatasetInfoPO po) {
    File trainFile = result.getTrainFile();
    int size = (int) (result.getTrainFileSize() > 10000 ? 10000 : result.getTrainFileSize());
    saveLabeledData(DatasetType.TRAIN, size, trainFile, fileTask.getTaskId(), po.getId());
  }

  private void saveTestData(FileTask fileTask, PreCheckResult result, DatasetInfoPO po) {
    File testFile = result.getTestFile();
    int size = (int) (result.getTestFileSize() > 10000 ? 10000 : result.getTestFileSize());
    saveLabeledData(DatasetType.TEST_UN_SHOW, size, testFile, fileTask.getTaskId(), po.getId());
  }

  private void saveLabeledData(
      DatasetType type, int size, File file, Long taskId, Integer datasetId) {
    log.info(
        "[DatasetParseInternal.saveLabeledData] ?????????????????????????????????????????????{}??????????????????{}??????",
        type.getName(),
        file.length());
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
      String line;
      List<TestDataPO> list = new ArrayList<>(size);
      long dataId = 0;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        TextLabelDataModel model = objectMapper.readValue(line, TextLabelDataModel.class);
        TestDataPO po = new TestDataPO();
        po.setTaskId(taskId);
        po.setDataSetId(datasetId);
        po.setDataId(dataId++);
        po.setLabel(model.getLabel());
        po.setSentence(model.getSentence());
        po.setLabelDes(model.getLabelDes());
        po.setDataType(type.getType());
        // ???????????????
        switch (type) {
          case TEST_UN_SHOW:
            po.setShowData(0);
            break;
          case TEST_SHOW:
          default:
            po.setShowData(1);
        }
        // ???????????????
        list.add(po);
        if (list.size() >= 10000) {
          testDataService.saveForBatchNoLog(list);
          list.clear();
        }
      }
      if (list.size() > 0) {
        testDataService.saveForBatchNoLog(list);
        list.clear();
      }
    } catch (IOException e) {
      throw new BusinessIOException("????????????????????????", e);
    }
    stopWatch.stop();
    log.info(
        "[DatasetParseInternal.saveLabeledData] ????????????????????????????????????{}???", stopWatch.getTotalTimeSeconds());
  }

  private void saveUnlabelData(FileTask fileTask, PreCheckResult result, DatasetInfoPO po) {
    File unlabelFile = result.getUnlabelFile();
    Long taskId = fileTask.getTaskId();
    Integer datasetId = po.getId();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(unlabelFile)))) {
      long dataId = 0;
      String line;
      int size = (int) (result.getUnlabelFileSize() > 10000 ? 10000 : result.getUnlabelFileSize());
      ArrayList<UnlabelDataPO> unlabelDetails = new ArrayList<>(size);
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        TextUnlabelModel model = objectMapper.readValue(line, TextUnlabelModel.class);
        UnlabelDataPO unlabelDataPO = new UnlabelDataPO();
        unlabelDataPO.setDataSetId(datasetId);
        unlabelDataPO.setSentence(model.getSentence());
        unlabelDataPO.setDataId(dataId++);
        unlabelDataPO.setTaskId(taskId);
        unlabelDetails.add(unlabelDataPO);
        if (unlabelDetails.size() >= 10000) {
          unlabelDataService.saveBatchNoLog(unlabelDetails);
          unlabelDetails.clear();
        }
      }
      if (unlabelDetails.size() > 0) {
        unlabelDataService.saveBatchNoLog(unlabelDetails);
        unlabelDetails.clear();
      }
    } catch (IOException e) {
      throw new BusinessIOException("????????????????????????", e);
    }
  }

  private void saveLabelInfo(FileTask fileTask, DatasetInfoPO po) {
    Map<String, Integer> currentLabel = LabelSupport.getCurrentLabel();
    List<LabelInfoPO> labels = new ArrayList<>(currentLabel.size());
    Long taskId = fileTask.getTaskId();
    for (Map.Entry<String, Integer> entry : currentLabel.entrySet()) {
      String labelName = entry.getKey();
      Integer labelId = entry.getValue();
      LabelInfoPO label = new LabelInfoPO();
      label.setLabelDesc(labelName);
      label.setLabelId(labelId);
      label.setTaskId(taskId);
      label.setDatasetId(po.getId());
      labels.add(label);
    }
    // ????????????
    labelInfoService.saveBatchNoLog(labels);
    log.info("[DatasetParseInternal.saveLabelInfo] ?????????????????????????????????????????????{}", labels.size());
  }

  private void removeOldData(Long taskId) {
    // ???????????????????????????
    int count = datasetInfoService.removeByTaskId(taskId);
    log.info("[DatasetParseHandler.removeOldData] ?????????????????????????????????????????????{}", count);
    // ?????????????????????
    count = labelInfoService.deleteLabelInfoByTaskId(taskId);
    log.info("[DatasetParseHandler.removeOldData] ???????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = unlabelDataService.deleteUnlabelDataByTaskId(taskId);
    log.info("[DatasetParseHandler.removeOldData] ????????????????????????????????????????????????{}", count);
    // ???????????????????????????
    count = testDataService.deleteTestDataByTaskId(taskId);
    log.info("[DatasetParseHandler.removeOldData] ?????????????????????????????????????????????{}", count);
    // ?????????????????????????????????????????????????????????
    count = ruleResultService.deleteAllByTaskId(taskId);
    log.info("[DatasetParseHandler.removeOldData] ?????????????????????????????????????????????{}", count);
    count = ruleUnlabeledResultService.deleteAllByTaskId(taskId);
    log.info("[DatasetParseHandler.removeOldData] ??????????????????????????????????????????????????????{}", count);
  }

  @Autowired
  public void setDatasetInfoService(DatasetInfoService datasetInfoService) {
    this.datasetInfoService = datasetInfoService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
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
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }
}
