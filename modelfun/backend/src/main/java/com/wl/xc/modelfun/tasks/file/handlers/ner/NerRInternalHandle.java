package com.wl.xc.modelfun.tasks.file.handlers.ner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.entities.model.DataSetParse;
import com.wl.xc.modelfun.entities.model.NerTestDataModel;
import com.wl.xc.modelfun.entities.model.NerTestDataModel.EntitiesDTO;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelPO;
import com.wl.xc.modelfun.entities.po.NerTestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.NerAutoLabelMapService;
import com.wl.xc.modelfun.service.NerAutoLabelResultService;
import com.wl.xc.modelfun.service.NerDataLabelService;
import com.wl.xc.modelfun.service.NerTestDataService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 * @version 1.0
 * @date 2022/6/7 16:24
 */
@Slf4j
@Component
public class NerRInternalHandle implements InternalHandle {

  private DatasetInfoService datasetInfoService;

  private UnlabelDataService unlabelDataService;

  private LabelInfoService labelInfoService;

  private DatasetDetailService datasetDetailService;

  private NerTestDataService nerTestDataService;

  private NerDataLabelService nerDataLabelService;

  private NerAutoLabelResultService autoLabelResultService;

  private NerAutoLabelMapService autoLabelMapService;

  private FileService fileService;

  private ObjectMapper objectMapper;

  private final Map<DatasetType, SaveFileData> consumerMap = new HashMap<>();

  public NerRInternalHandle() {
    consumerMap.put(DatasetType.UNLABELED, this::saveUnlabelData);
    consumerMap.put(DatasetType.TEST, this::saveLabeledData);
    consumerMap.put(DatasetType.TRAIN, this::saveLabeledData);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void internalHandle(FileTask fileTask) {
    PreCheckResult result = (PreCheckResult) fileTask.getConfig().get("PreCheckResult");
    removeOldData(fileTask.getTaskId());
    // ????????????????????????????????????
    DatasetInfoPO po = new DatasetInfoPO();
    po.setName(fileTask.getFileName());
    po.setTaskId(fileTask.getTaskId());
    po.setDatasetAddress(fileTask.getPath());
    po.setCreatePoeple(fileTask.getCreatePeople());
    datasetInfoService.save(po);
    saveData(result, po);
    uploadFiles(result, fileTask, po.getId());
  }

  private void saveData(PreCheckResult result, DatasetInfoPO po) {
    Map<String, Integer> labelMap = LabelSupport.getCurrentLabel();
    // ???????????????
    saveLabel(labelMap, po);
    // ??????????????????
    saveByFile(result.getUnlabelFile(), result.getUnlabelFileSize(), po, DatasetType.UNLABELED);
    // ???????????????
    saveByFile(result.getTestFile(), result.getTestFileSize(), po, DatasetType.TEST);
    // ???????????????
    saveByFile(result.getTrainFile(), result.getTrainFileSize(), po, DatasetType.TRAIN);
  }

  private void saveByFile(File file, long length, DatasetInfoPO po, DatasetType type) {
    // ???10000???????????????????????????
    log.info("[NerRInternalHandle.saveByFile] ??????????????????????????????????????????{}???size={}", file.getName(), length);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    consumerMap.get(type).saveFileData(file, (int) length, po);
    stopWatch.stop();
    log.info("[NerRInternalHandle.saveByFile] ????????????????????????????????????{}???", stopWatch.getTotalTimeSeconds());
  }

  private void saveUnlabelData(File file, int length, DatasetInfoPO entity) {
    ArrayList<UnlabelDataPO> unlabelDetails = new ArrayList<>(Math.min(length, 10000));
    long lineNum = 0;
    long realLine = 0;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        DataSetParse parse = objectMapper.readValue(line, DataSetParse.class);
        lineNum++;
        UnlabelDataPO po = new UnlabelDataPO();
        po.setDataSetId(entity.getId());
        po.setSentence(parse.getSentence());
        po.setDataId(realLine++);
        po.setTaskId(entity.getTaskId());
        unlabelDetails.add(po);
        if (lineNum % 10000 == 0) {
          unlabelDataService.saveBatchNoLog(unlabelDetails);
          unlabelDetails.clear();
          lineNum = 0;
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("??????????????????", e);
    }
    if (lineNum > 0) {
      unlabelDataService.saveBatchNoLog(unlabelDetails);
    }
  }

  private void saveLabeledData(File file, int length, DatasetInfoPO entity) {
    ArrayList<NerTestDataPO> testDetails = new ArrayList<>(length);
    ArrayList<NerDataLabelPO> dataLabelPOS = new ArrayList<>(length * 2);
    Map<String, Integer> map = LabelSupport.getCurrentLabel();
    DatasetType type = DatasetType.getFromName(file.getName());
    if (DatasetType.TEST_UN_SHOW == type) {
      type = DatasetType.TEST;
    }
    int typeId = type.getType();
    long realLine = 0;
    long entityNum = 0;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        NerTestDataModel model = objectMapper.readValue(line, NerTestDataModel.class);
        NerTestDataPO po = new NerTestDataPO();
        po.setRelations(null);
        po.setDataId(realLine);
        po.setTaskId(entity.getTaskId());
        po.setShowData(1);
        po.setDataType(typeId);
        po.setSentence(model.getText());
        testDetails.add(po);
        List<EntitiesDTO> entities = model.getEntities();
        realLine++;
        if (entities == null || entities.isEmpty()) {
          continue;
        }
        for (EntitiesDTO entityDTO : entities) {
          NerDataLabelPO labelPO = new NerDataLabelPO();
          labelPO.setTaskId(entity.getTaskId());
          labelPO.setDataType(typeId);
          labelPO.setStartOffset(entityDTO.getStartOffset());
          labelPO.setEndOffset(entityDTO.getEndOffset());
          labelPO.setSentenceId(po.getDataId());
          labelPO.setLabelId(map.get(entityDTO.getLabel()));
          labelPO.setDataId(entityNum++);
          dataLabelPOS.add(labelPO);
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("??????????????????", e);
    }
    if (realLine > 0) {
      // ????????????
      nerTestDataService.saveForBatchNoLog(testDetails);
      if (dataLabelPOS.size() > 0) {
        nerDataLabelService.saveForBatchNoLog(dataLabelPOS);
      }
    }
  }

  private void saveLabel(Map<String, Integer> labelMap, DatasetInfoPO entity) {
    ArrayList<LabelInfoPO> labelDetails = new ArrayList<>(labelMap.size());
    for (Map.Entry<String, Integer> entry : labelMap.entrySet()) {
      LabelInfoPO po = new LabelInfoPO();
      po.setTaskId(entity.getTaskId());
      po.setDatasetId(entity.getId());
      po.setLabelId(entry.getValue());
      po.setLabelDesc(entry.getKey());
      labelDetails.add(po);
    }
    labelInfoService.saveBatchNoLog(labelDetails);
  }

  private void uploadFiles(PreCheckResult result, FileTask fileTask, Integer datasetId) {
    List<File> files = new ArrayList<>(4);
    files.add(result.getLabelFile());
    files.add(result.getTrainFile());
    files.add(result.getTestFile());
    files.add(result.getUnlabelFile());
    fileService.uploadDatasetFiles(fileTask, files, datasetId, null, null);
  }

  /**
   * ????????????id????????????????????????????????????????????????????????????????????????
   *
   * @param taskId ??????id
   */
  private void removeOldData(Long taskId) {
    // ???????????????????????????
    int count = datasetInfoService.removeByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ?????????????????????????????????????????????{}", count);
    // ???????????????????????????
    count = datasetDetailService.removeByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ?????????????????????????????????????????????{}", count);
    // ?????????????????????
    count = labelInfoService.deleteLabelInfoByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ???????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = unlabelDataService.deleteUnlabelDataByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ????????????????????????????????????????????????{}", count);
    // ?????????????????????
    count = nerTestDataService.deleteByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ?????????????????????????????????????????????????????????{}", count);
    // ???????????????????????????????????????
    count = nerDataLabelService.deleteAllByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ???????????????????????????????????????????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = autoLabelResultService.deleteByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ????????????????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = autoLabelMapService.deleteByTaskId(taskId);
    log.info("[NerRInternalHandle.removeOldData] ????????????????????????????????????????????????{}", count);
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
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }

  @Autowired
  public void setNerTestDataService(NerTestDataService nerTestDataService) {
    this.nerTestDataService = nerTestDataService;
  }

  @Autowired
  public void setNerDataLabelService(NerDataLabelService nerDataLabelService) {
    this.nerDataLabelService = nerDataLabelService;
  }

  @Autowired
  public void setAutoLabelResultService(NerAutoLabelResultService autoLabelResultService) {
    this.autoLabelResultService = autoLabelResultService;
  }

  @Autowired
  public void setAutoLabelMapService(NerAutoLabelMapService autoLabelMapService) {
    this.autoLabelMapService = autoLabelMapService;
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
