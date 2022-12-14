package com.wl.xc.modelfun.tasks.file.handlers;

import static com.wl.xc.modelfun.commons.FileConstant.NER_TUNE_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TEST_DATA_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.DataSetParse;
import com.wl.xc.modelfun.entities.model.DataSetParseResult;
import com.wl.xc.modelfun.entities.model.NerLabelModel;
import com.wl.xc.modelfun.entities.model.NerTestDataModel;
import com.wl.xc.modelfun.entities.model.NerTestDataModel.EntitiesDTO;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelPO;
import com.wl.xc.modelfun.entities.po.NerTestDataPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.NerAutoLabelMapService;
import com.wl.xc.modelfun.service.NerAutoLabelResultService;
import com.wl.xc.modelfun.service.NerDataLabelService;
import com.wl.xc.modelfun.service.NerTestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 * @version 1.0
 * @date 2022/4/13 15:35
 */
@Slf4j
public class NerParseHandler extends AbstractFileUploadTaskHandler {

  private UnlabelDataService unlabelDataService;

  private LabelInfoService labelInfoService;

  private DatasetDetailService datasetDetailService;

  private NerTestDataService nerTestDataService;

  private NerDataLabelService nerDataLabelService;

  private NerAutoLabelResultService autoLabelResultService;

  private NerAutoLabelMapService autoLabelMapService;

  private NerInternal nerInternal;

  private FileService fileService;

  private final Map<DatasetType, SaveContent> consumerMap = new HashMap<>();

  private final ThreadLocal<Set<Long>> IDS_HOLDER = new ThreadLocal<>();

  private final ThreadLocal<Map<String, Integer>> OLD_LABEL_HOLDER = new ThreadLocal<>();

  private final ThreadLocal<Map<String, Integer>> NEW_LABEL_HOLDER = new ThreadLocal<>();


  public NerParseHandler() {
    consumerMap.put(DatasetType.TEST, new SaveTestContent());
    consumerMap.put(DatasetType.UNLABELED, new SaveTrainContent());
    consumerMap.put(DatasetType.NER_LABEL, new SaveLabelContent());
  }

  @Override
  public FileTaskType getType() {
    return FileTaskType.NER;
  }

  @Override
  protected void internalHandle(FileTask fileTask) {
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File fileDir = file.getParentFile();
    List<File> files = getLocalFiles(fileTask.getLocalPath());
    if (files == null || files.isEmpty()) {
      log.error("[NerParseHandler.handle] ????????????????????????");
      handleTaskError(fileTask.getRequestId(), fileTask, "????????????????????????");
      return;
    }
    if (files.size() != 3) {
      throw new BusinessIllegalStateException("??????????????????????????????????????????????????????????????????????????????????????????????????????");
    }
    // ????????????????????????????????????????????????
    files.sort(this::compare);
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(fileTask.getTaskId());
    List<DataSetParseResult> parseResults;
    if (labelInfoPOS.size() > 0) {
      Map<String, Integer> OldLabelMap = labelInfoPOS.stream()
          .collect(Collectors.toMap(LabelInfoPO::getLabelDesc, LabelInfoPO::getLabelId));
      OLD_LABEL_HOLDER.set(OldLabelMap);
    }
    parseResults = new ArrayList<>(3);
    List<File> generateFiles = new ArrayList<>(3);
    File testFile = null;
    for (File unzipFile : files) {
      DataSetParseResult result = parseFile(unzipFile);
      parseResults.add(result);
      generateFiles.add(result.getFile());
      if (unzipFile.getName().contains(TEST_DATA_NAME)) {
        testFile = result.getFile();
      }
      result.setFile(null);
    }
    if (testFile == null) {
      throw new BusinessIllegalStateException("????????????????????????");
    }
    files.clear();
    // ????????????
    List<File> list;
    String showFileName;
    String unShowFileName;
    list = shardingFile(testFile, fileDir);
    showFileName = FileUtil.mainName(testFile) + "_show.json";
    unShowFileName = FileUtil.mainName(testFile) + "_unShow.json";
    fileTask.getConfig().put("parseResults", parseResults);
    fileTask.getConfig().put("fileList", generateFiles);
    fileTask.getConfig().put("shardingList", list);
    fileTask.getConfig().put("showFileName", showFileName);
    fileTask.getConfig().put("unShowFileName", unShowFileName);
    nerInternal.internalHandle(fileTask);
    log.info("[NerParseHandler.internalHandle] end");
  }

  /**
   * ?????????????????????nerdata??????????????????????????????
   *
   * @param file1 ??????1
   * @param file2 ??????2
   * @return ????????????
   */
  private int compare(File file1, File file2) {
    if (file1.getName().contains("nerdata")) {
      return -1;
    } else if (file2.getName().contains("nerdata")) {
      return 1;
    } else {
      return file1.getName().compareTo(file2.getName());
    }
  }

  @Override
  protected void finallyOp(FileTask fileTask) {
    OLD_LABEL_HOLDER.remove();
    NEW_LABEL_HOLDER.remove();
    IDS_HOLDER.remove();
  }

  @Override
  protected void handleTaskSuccess(String requestId, FileTask fileTask) {
    super.handleTaskSuccess(requestId, fileTask);
  }

  @Override
  protected void handleTaskError(String requestId, FileTask fileTask, String errorMsg) {
    // ??????oss??????
    ossService.deleteFile(fileTask.getPath());
    // ?????????oss??????????????????????????????????????????????????????
    // ??????websocket???????????????????????????????????????????????????
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.DATASET_PARSE_FAIL);
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(infoPO.getId(), infoPO.getName(),
            String.format("?????????%s?????????NER?????????????????????????????????????????????%s", infoPO.getName(), errorMsg),
            false));
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  public void afterHandle(FileTask fileTask) {
    // ??????websocket???????????????????????????
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.DATASET_PARSE_SUCCESS);
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(
            infoPO.getId(),
            infoPO.getName(),
            String.format("?????????%s????????????NER???????????????????????????", infoPO.getName()),
            true));
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected String getTaskCacheKey(FileTask fileTask) {
    return RedisKeyMethods.generateDatasetKey(fileTask.getTaskId());
  }

  /**
   * ????????????id????????????????????????????????????????????????????????????????????????
   *
   * @param taskId ??????id
   */
  private void removeOldData(Long taskId) {
    // ???????????????????????????
    int count = datasetInfoService.removeByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ?????????????????????????????????????????????{}", count);
    // ???????????????????????????
    count = datasetDetailService.removeByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ?????????????????????????????????????????????{}", count);
    // ?????????????????????
    count = labelInfoService.deleteLabelInfoByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ???????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = unlabelDataService.deleteUnlabelDataByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ????????????????????????????????????????????????{}", count);
    // ?????????????????????
    count = nerTestDataService.deleteByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ?????????????????????????????????????????????{}", count);
    // ???????????????????????????????????????
    count = nerDataLabelService.deleteAllByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ???????????????????????????????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = autoLabelResultService.deleteByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ????????????????????????????????????????????????{}", count);
    // ??????????????????????????????
    count = autoLabelMapService.deleteByTaskId(taskId);
    log.info("[NerParseHandler.removeOldData] ????????????????????????????????????????????????{}", count);
  }

  /**
   * ???????????????????????????????????????????????????
   *
   * @param file ??????
   * @return ????????????
   */
  protected DataSetParseResult parseFile(File file) {
    int count = 0;
    int length = 0;
    DatasetType datasetType = DatasetType.getFromName(file.getName());
    if (datasetType.equals(DatasetType.NER_LABEL)) {
      Map<String, Integer> map = new HashMap<>();
      NEW_LABEL_HOLDER.set(map);
    }
    File generatedFile = null;
    try (FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
      BufferedWriter writer = null;
      try {
        if (datasetType.equals(DatasetType.TEST)) {
          String generateName = FileUtil.mainName(file) + "_generate.json";
          generatedFile = new File(file.getParentFile(), generateName);
          writer = new BufferedWriter(new FileWriter(generatedFile));
        }
        String line;
        while ((line = reader.readLine()) != null) {
          count++;
          if (line.isEmpty()) {
            continue;
          }
          try {
            boolean b = validLine(line, datasetType);
            if (b && datasetType.equals(DatasetType.TEST)) {
              length++;
              // ?????????????????????????????????????????????????????????????????????
              String writeLine = getTestLine(line);
              writer.write(writeLine);
              writer.newLine();
            }
          } catch (JsonProcessingException e) {
            throw new BusinessArgumentException("??????" + file.getName() + "???" + count + "????????????????????????");
          } catch (BusinessArgumentException e) {
            throw new BusinessArgumentException(
                "??????" + file.getName() + "???" + count + "????????????????????????" + e.getMessage());
          }
        }
      } finally {
        if (writer != null) {
          writer.close();
        }
      }
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("??????????????????");
    }
    if (datasetType.equals(DatasetType.NER_LABEL) && OLD_LABEL_HOLDER.get() != null) {
      validLabel();
    }
    if (datasetType.equals(DatasetType.TEST) && length == 0) {
      throw new BusinessArgumentException(
          "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
    }
    DataSetParseResult parseResult = new DataSetParseResult();
    if (generatedFile != null) {
      parseResult.setFile(generatedFile);
      parseResult.setFileName(generatedFile.getName());
    } else {
      parseResult.setFile(file);
      parseResult.setFileName(file.getName());
    }
    parseResult.setSetType(datasetType);
    parseResult.setLength(length);
    return parseResult;
  }

  private String getTestLine(String line) throws JsonProcessingException {
    NerTestDataModel model = objectMapper.readValue(line, NerTestDataModel.class);
    validEntities(model);
    return objectMapper.writeValueAsString(model);
  }

  private boolean validLine(String line, DatasetType datasetType) throws JsonProcessingException {
    switch (datasetType) {
      case TEST:
        return validTestData(line);
      case UNLABELED:
        validUnlabeledData(line);
        break;
      case NER_LABEL:
        validLabelData(line);
        break;
      default:
        throw new BusinessIllegalStateException("????????????????????????");
    }
    return true;
  }

  private boolean validTestData(String line) throws JsonProcessingException {
    NerTestDataModel model = objectMapper.readValue(line, NerTestDataModel.class);
    if (StringUtils.isBlank(model.getText())) {
      throw new BusinessArgumentException("??????????????????????????????");
    }
    if (model.getEntities() == null || model.getEntities().isEmpty()) {
      throw new BusinessArgumentException("????????????????????????????????????");
    }
    for (NerTestDataModel.EntitiesDTO entity : model.getEntities()) {
      if (StringUtils.isBlank(entity.getLabel())) {
        throw new BusinessArgumentException("????????????????????????????????????");
      }
      if (entity.getStartOffset() == null || entity.getEndOffset() == null) {
        throw new BusinessArgumentException("???????????????????????????????????????????????????????????????");
      }
    }
    return validEntities(model);
  }

  private void validUnlabeledData(String line) throws JsonProcessingException {
    DataSetParse parse = objectMapper.readValue(line, DataSetParse.class);
    if (StringUtils.isBlank(parse.getSentence())) {
      throw new BusinessArgumentException("??????????????????????????????????????????");
    }
  }

  private void validLabelData(String line) throws JsonProcessingException {
    NerLabelModel model = objectMapper.readValue(line, NerLabelModel.class);
    if (!StringUtils.isNumeric(model.getNer())) {
      throw new BusinessArgumentException("??????ID?????????????????????????????????");
    }
    if (StringUtils.isBlank(model.getNerDes())) {
      throw new BusinessArgumentException("???????????????????????????");
    }
    NEW_LABEL_HOLDER.get().put(model.getNerDes(), Integer.parseInt(model.getNer()));
  }

  protected void saveData(List<File> files, DatasetInfoPO entity, List<DataSetParseResult> parseResults) {
    Map<String, DataSetParseResult> map = parseResults.stream()
        .collect(Collectors.toMap(DataSetParseResult::getFileName, Function.identity()));
    for (File file : files) {
      String name = file.getName();
      DataSetParseResult parseResult = map.get(name);
      // ???10000???????????????????????????
      log.info("[NerParseHandler.saveData] ??????????????????????????????????????????{}???size={}", name, parseResult.getLength());
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      DatasetType setType = parseResult.getSetType();
      consumerMap.get(setType).saveContent(file, entity, parseResult);
      stopWatch.stop();
      log.info("[NerParseHandler.saveData] ????????????????????????????????????{}???", stopWatch.getTotalTimeSeconds());
    }
  }

  private List<File> shardingFile(File testFile, File fileDir) {
    List<NerTestDataModel> testList = new ArrayList<>();
    Map<String, NerTestDataModel> map = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)))) {
      String line;
      long lineNum = 0;
      while ((line = reader.readLine()) != null) {
        NerTestDataModel parse = objectMapper.readValue(line, NerTestDataModel.class);
        lineNum++;
        parse.setId(lineNum);
        testList.add(parse);
        List<EntitiesDTO> entities = parse.getEntities();
        for (EntitiesDTO entity : entities) {
          if (!map.containsKey(entity.getLabel())) {
            NerTestDataModel model = new NerTestDataModel();
            model.setText(parse.getText());
            model.setId(parse.getId());
            model.setRelations(parse.getRelations());
            model.setEntities(Collections.singletonList(entity));
            map.put(entity.getLabel(), model);
          }
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("??????????????????", e);
    }
    int nerSliceRate = fileUploadProperties.getNerSliceRate();
    String showNum = CalcUtil.multiply(testList.size() + "", CalcUtil.divide(nerSliceRate, 100, 2));
    int showNumInt = new BigDecimal(showNum).intValue();
    List<NerTestDataModel> showList = new ArrayList<>(showNumInt);
    List<NerTestDataModel> unShowList = new ArrayList<>(testList.size() - showNumInt);
    // ????????????showNumInt?????????
    Set<Long> showIds = new HashSet<>(showNumInt);
    Random random = new SecureRandom();
    while (showIds.size() < showNumInt) {
      showIds.add(testList.get(random.nextInt(testList.size())).getId());
    }
    for (NerTestDataModel test : testList) {
      if (showIds.contains(test.getId())) {
        showList.add(test);
      } else {
        unShowList.add(test);
      }
    }
    IDS_HOLDER.set(showIds);
    File showFile = new File(fileDir, FileUtil.mainName(testFile) + "_show.json");
    File unShowFile = new File(fileDir, FileUtil.mainName(testFile) + "_unShow.json");
    unShowList.sort(Comparator.comparingLong(NerTestDataModel::getId));
    showList.sort(Comparator.comparingLong(NerTestDataModel::getId));
    // ???????????????????????????
    writeFile(unShowList, unShowFile);
    // ?????????????????????
    File tuneFile = new File(fileDir, NER_TUNE_NAME + ".json");
    writeFile(getTuneList(map), tuneFile);
    if (showList.size() > 0) {
      writeFile(showList, showFile);
      return Arrays.asList(showFile, unShowFile, tuneFile);
    } else {
      return Arrays.asList(unShowFile, tuneFile);
    }
  }

  private List<NerTestDataModel> getTuneList(Map<String, NerTestDataModel> map) {
    List<NerTestDataModel> tuneList = new ArrayList<>();
    Set<Entry<String, Integer>> entrySet = NEW_LABEL_HOLDER.get().entrySet();
    int entityNum = 0;
    long modelNum = 0;
    for (Entry<String, Integer> entry : entrySet) {
      String label = entry.getKey();
      if (map.containsKey(label)) {
        NerTestDataModel model = map.get(label);
        model.setId(modelNum++);
        model.getEntities().get(0).setId(entityNum++);
        tuneList.add(model);
        continue;
      }
      String text = String.format("????????????%s???????????????", label);
      NerTestDataModel model = new NerTestDataModel();
      model.setId(modelNum++);
      model.setText(text);
      model.setRelations(Collections.emptyList());
      EntitiesDTO dto = new EntitiesDTO();
      dto.setLabel(label);
      dto.setId(entityNum++);
      dto.setStartOffset(4);
      dto.setEndOffset(4 + label.length());
      model.setEntities(Collections.singletonList(dto));
      tuneList.add(model);
    }
    return tuneList;
  }

  private void writeFile(List<NerTestDataModel> writeList, File writeFile) {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeFile)))) {
      for (int i = 0; i < writeList.size(); i++) {
        NerTestDataModel model = writeList.get(i);
        writer.write(objectMapper.writeValueAsString(model));
        if (i != writeList.size() - 1) {
          writer.newLine();
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("??????????????????", e);
    }
  }

  /**
   * ??????????????????????????????????????????
   * <p>
   * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
   * <p>
   * ?????????????????????
   *
   * @param testData ??????????????????
   * @return ????????????
   */
  private boolean validEntities(NerTestDataModel testData) {
    List<EntitiesDTO> entities = testData.getEntities();
    Map<String, Integer> map = NEW_LABEL_HOLDER.get();
    entities.removeIf(e -> !map.containsKey(e.getLabel()));
    return entities.size() > 0;
  }

  private void validLabel() {
    Map<String, Integer> oldLabel = OLD_LABEL_HOLDER.get();
    Map<String, Integer> newLabel = NEW_LABEL_HOLDER.get();
    // ???????????????????????????????????????????????????????????????????????????????????????????????????
    for (Entry<String, Integer> entry : oldLabel.entrySet()) {
      if (!newLabel.containsKey(entry.getKey())) {
        throw new BusinessException("????????????????????????????????????????????????????????????????????????????????????");
      } else {
        if (!entry.getValue().equals(newLabel.get(entry.getKey()))) {
          throw new BusinessException("????????????????????????????????????????????????????????????????????????????????????");
        }
      }
    }
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
  public void setNerInternal(NerInternal nerInternal) {
    this.nerInternal = nerInternal;
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
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public class NerInternal implements InternalHandle {

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void internalHandle(FileTask fileTask) {
      List<DataSetParseResult> parseResults = (List<DataSetParseResult>) fileTask.getConfig().get("parseResults");
      List<File> files = (List<File>) fileTask.getConfig().get("fileList");
      String showFileName = (String) fileTask.getConfig().get("showFileName");
      String unShowFileName = (String) fileTask.getConfig().get("unShowFileName");
      List<File> list = (List<File>) fileTask.getConfig().get("shardingList");
      // ????????????????????????????????????
      removeOldData(fileTask.getTaskId());
      // ????????????????????????????????????
      DatasetInfoPO po = new DatasetInfoPO();
      po.setDatasetAddress(fileTask.getPath());
      po.setCreatePoeple(fileTask.getCreatePeople());
      po.setName(fileTask.getFileName());
      po.setTaskId(fileTask.getTaskId());
      datasetInfoService.save(po);
      // ????????????
      saveData(files, po, parseResults);
      files.addAll(list);
      fileService.uploadDatasetFiles(fileTask, files, po.getId(), showFileName, unShowFileName);
    }
  }

  private class SaveTestContent implements SaveContent {

    @Override
    public void saveContent(File file, DatasetInfoPO entity, DataSetParseResult result) {
      ArrayList<NerTestDataPO> testDetails = new ArrayList<>(result.getLength());
      ArrayList<NerDataLabelPO> dataLabelPOS = new ArrayList<>(result.getLength() * 2);
      Map<String, Integer> map = NEW_LABEL_HOLDER.get();
      Set<Long> showIds = IDS_HOLDER.get();
      long realLine = 0;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
        String line;
        while ((line = reader.readLine()) != null) {
          NerTestDataModel model = objectMapper.readValue(line, NerTestDataModel.class);
          realLine++;
          NerTestDataPO po = new NerTestDataPO();
          po.setRelations(null);
          po.setSentence(model.getText());
          po.setDataId(realLine);
          po.setTaskId(entity.getTaskId());
          if (showIds.contains(po.getDataId())) {
            po.setShowData(1);
          } else {
            po.setShowData(0);
          }
          testDetails.add(po);
          List<EntitiesDTO> entities = model.getEntities();
          for (EntitiesDTO entityDTO : entities) {
            NerDataLabelPO labelPO = new NerDataLabelPO();
            labelPO.setTaskId(entity.getTaskId());
            labelPO.setSentenceId(realLine);
            labelPO.setLabelId(map.get(entityDTO.getLabel()));
            labelPO.setStartOffset(entityDTO.getStartOffset());
            labelPO.setEndOffset(entityDTO.getEndOffset());
            labelPO.setDataId(entityDTO.getId() == null ? null : entityDTO.getId().longValue());
            dataLabelPOS.add(labelPO);
          }
        }
      } catch (IOException e) {
        throw new BusinessIOException("??????????????????", e);
      }
      if (realLine > 0) {
        // ????????????
        nerTestDataService.saveForBatchNoLog(testDetails);
        nerDataLabelService.saveForBatchNoLog(dataLabelPOS);
      }
    }
  }

  private class SaveTrainContent implements SaveContent {

    @Override
    public void saveContent(File file, DatasetInfoPO entity, DataSetParseResult result) {
      ArrayList<UnlabelDataPO> unlabelDetails = new ArrayList<>(Math.min(result.getLength(), 10000));
      long lineNum = 0;
      long realLine = 0;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isEmpty()) {
            continue;
          }
          DataSetParse parse = objectMapper.readValue(line, DataSetParse.class);
          lineNum++;
          realLine++;
          UnlabelDataPO po = new UnlabelDataPO();
          po.setDataSetId(entity.getId());
          po.setSentence(parse.getSentence());
          po.setDataId(realLine);
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
  }

  private class SaveLabelContent implements SaveContent {

    @Override
    public void saveContent(File file, DatasetInfoPO entity, DataSetParseResult result) {
      ArrayList<LabelInfoPO> labelDetails = new ArrayList<>(result.getLength());
      long lineNum = 0;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isEmpty()) {
            continue;
          }
          NerLabelModel model = objectMapper.readValue(line, NerLabelModel.class);
          lineNum++;
          LabelInfoPO po = new LabelInfoPO();
          po.setTaskId(entity.getTaskId());
          po.setDatasetId(entity.getId());
          po.setLabelId(Integer.parseInt(model.getNer()));
          po.setLabelDesc(model.getNerDes());
          labelDetails.add(po);
        }
      } catch (IOException e) {
        throw new BusinessIOException("??????????????????", e);
      }
      if (lineNum > 0) {
        labelInfoService.saveBatchNoLog(labelDetails);
      }
    }
  }
}
