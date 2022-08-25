package com.wl.xc.modelfun.tasks.file.handlers;

import static com.wl.xc.modelfun.commons.FileConstant.LABEL_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TEST_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TEXT_EXCEL_LABEL;
import static com.wl.xc.modelfun.commons.FileConstant.TEXT_EXCEL_TEST;
import static com.wl.xc.modelfun.commons.FileConstant.TEXT_EXCEL_TRAIN;
import static com.wl.xc.modelfun.commons.FileConstant.TEXT_EXCEL_UNLABEL;
import static com.wl.xc.modelfun.commons.FileConstant.TRAIN_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.UN_LABEL_DATA_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.enums.FileTaskType.HF_WORD;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.FileTaskAppendEvent;
import com.wl.xc.modelfun.tasks.file.handlers.text.DatasetParseInternal;
import com.wl.xc.modelfun.tasks.file.handlers.text.LabelSheetReadListener;
import com.wl.xc.modelfun.tasks.file.handlers.text.TestDataSheetListener;
import com.wl.xc.modelfun.tasks.file.handlers.text.TextLabelDataModel;
import com.wl.xc.modelfun.tasks.file.handlers.text.TextLabelModel;
import com.wl.xc.modelfun.tasks.file.handlers.text.TextUnlabelModel;
import com.wl.xc.modelfun.tasks.file.handlers.text.TrainDataSheetListener;
import com.wl.xc.modelfun.tasks.file.handlers.text.UnlabelSheetListener;
import com.wl.xc.modelfun.tasks.rule.RuleTask;
import com.wl.xc.modelfun.tasks.rule.RuleTaskAppendEvent;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @version 1.0
 * @date 2022/4/13 15:35
 */
@Slf4j
public class DatasetParseHandler extends AbstractFileUploadTaskHandler {

  private LabelInfoService labelInfoService;

  private ApplicationEventPublisher eventPublisher;

  private DatasetParseInternal internalHandle;

  private final Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");

  private final ThreadLocal<Set<Long>> IDS_HOLDER = new ThreadLocal<>();

  private final ThreadLocal<Map<Integer, String>> OLD_LABEL_HOLDER = new ThreadLocal<>();

  private final ThreadLocal<Map<Integer, String>> NEW_LABEL_HOLDER = new ThreadLocal<>();

  private final Map<String, BiFunction<FileTask, File, PreCheckResult>> FILE_OP = new HashMap<>();

  public DatasetParseHandler() {
    FILE_OP.put("xlsx", this::parseExcel);
    FILE_OP.put("xls", this::parseExcel);
    FILE_OP.put("zip", this::parseZip);
  }

  @Override
  public FileTaskType getType() {
    return FileTaskType.DATASET;
  }

  @Override
  protected void internalHandle(FileTask fileTask) {
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    String extName = FileUtil.extName(file);
    BiFunction<FileTask, File, PreCheckResult> fileHandler = FILE_OP.get(extName);
    if (fileHandler == null) {
      throw new BusinessIllegalStateException("不支持的文件类型");
    }
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(fileTask.getTaskId());
    if (labelInfoPOS.size() > 0) {
      Map<Integer, String> OldLabelMap =
          labelInfoPOS.stream()
              .collect(Collectors.toMap(LabelInfoPO::getLabelId, LabelInfoPO::getLabelDesc));
      OLD_LABEL_HOLDER.set(OldLabelMap);
    }
    PreCheckResult result = fileHandler.apply(fileTask, file);
    fileTask.getConfig().put("PreCheckResult", result);
    internalHandle.internalHandle(fileTask);
    log.info("[DatasetParseHandler.internalHandle] end");
  }

  private PreCheckResult parseExcel(FileTask fileTask, File file) {
    ExcelReaderBuilder readerBuilder = EasyExcel.read(file);
    PreCheckResult result = new PreCheckResult();
    try (ExcelReader excelReader = readerBuilder.build()) {
      // 获取sheets列表。判断是否存在符合名称的sheet
      List<ReadSheet> readSheets = excelReader.excelExecutor().sheetList();
      ReadSheet unlabelSheet = null;
      ReadSheet testSheet = null;
      ReadSheet trainSheet = null;
      ReadSheet labelSheet = null;
      Set<String> sheetNames = new HashSet<>();
      for (ReadSheet readSheet : readSheets) {
        if (TEXT_EXCEL_UNLABEL.equals(readSheet.getSheetName())
            && !sheetNames.contains(TEXT_EXCEL_UNLABEL)) {
          unlabelSheet = readSheet;
          result.setHasUnlabelFile(true);
          sheetNames.add(TEXT_EXCEL_UNLABEL);
        } else if (TEXT_EXCEL_TEST.equals(readSheet.getSheetName())
            && !sheetNames.contains(TEXT_EXCEL_TEST)) {
          testSheet = readSheet;
          result.setHasTestFile(true);
          sheetNames.add(TEXT_EXCEL_TEST);
        } else if (TEXT_EXCEL_TRAIN.equals(readSheet.getSheetName())
            && !sheetNames.contains(TEXT_EXCEL_TRAIN)) {
          trainSheet = readSheet;
          result.setHasTrainFile(true);
          sheetNames.add(TEXT_EXCEL_TRAIN);
        } else if (TEXT_EXCEL_LABEL.equals(readSheet.getSheetName())
            && !sheetNames.contains(TEXT_EXCEL_LABEL)) {
          labelSheet = readSheet;
          result.setHasLabelFile(true);
          sheetNames.add(TEXT_EXCEL_LABEL);
        }
      }
      if (unlabelSheet == null || testSheet == null || labelSheet == null) {
        throw new BusinessIllegalStateException("Excel中必须存在未标注集、测试集和标签集的表单");
      }
      // 先解析标签集，和原有标签进行对比
      parseLabelFromExcel(excelReader, labelSheet, fileTask, result);
      // 再解析未标注集
      parseUnlabelFromExcel(excelReader, unlabelSheet, fileTask, result);
      // 如果训练集存在，则单独解析训练集
      if (trainSheet != null) {
        parseTrainFromExcel(excelReader, trainSheet, fileTask, result);
      }
      // 解析测试集，如果训练集不存在，则需要从测试集拆分
      parseTestFromExcel(excelReader, testSheet, fileTask, result);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessIllegalStateException("Excel解析异常", e);
    }
    return result;
  }

  private void parseTestFromExcel(
      ExcelReader excelReader, ReadSheet testSheet, FileTask fileTask, PreCheckResult result) {
    testSheet.setClazz(TextLabelDataModel.class);
    // 下载的文件
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File testFile = new File(file.getParentFile(), "testdata.json");
    File valFile = new File(file.getParentFile(), "valdata.json");
    try (BufferedWriter testWriter =
        Files.newBufferedWriter(testFile.toPath(), StandardCharsets.UTF_8);
        BufferedWriter valWriter =
            Files.newBufferedWriter(valFile.toPath(), StandardCharsets.UTF_8)) {
      TestDataSheetListener listener =
          new TestDataSheetListener(testWriter, valWriter, objectMapper);
      testSheet.getCustomReadListenerList().add(listener);
      BufferedWriter trainWriter = null;
      try {
        if (!result.isHasTrainFile()) {
          File trainFile = new File(file.getParentFile(), "traindata.json");
          trainWriter = Files.newBufferedWriter(trainFile.toPath(), StandardCharsets.UTF_8);
          listener.setTrainFileWriter(trainWriter);
          // 表示没有训练集，需要从测试集拆分
          listener.setHasTrainFile(false);
          result.setTrainFile(trainFile);
        }
        excelReader.read(testSheet);
        result.setTestFile(testFile);
        result.setValFile(valFile);
        result.setTestFileSize(listener.getTestLength());
        result.setValFileSize(listener.getValLength());
        if (trainWriter != null) {
          result.setTrainFileSize(listener.getTrainLength());
        }
      } finally {
        if (trainWriter != null) {
          trainWriter.close();
        }
      }
    } catch (IOException e) {
      throw new BusinessIllegalStateException("解析测试集失败", e);
    }
    if (result.getTestFileSize() == 0) {
      throw new BusinessIllegalStateException("解析得到的测试集数据为空");
    }
  }

  private void parseTrainFromExcel(
      ExcelReader excelReader, ReadSheet trainSheet, FileTask fileTask, PreCheckResult result) {
    trainSheet.setClazz(TextLabelDataModel.class);
    // 下载的文件
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File generatedFile = new File(file.getParentFile(), "traindata.json");
    generatedFile.deleteOnExit();
    try (BufferedWriter writer =
        Files.newBufferedWriter(generatedFile.toPath(), StandardCharsets.UTF_8)) {
      TrainDataSheetListener listener = new TrainDataSheetListener(writer, objectMapper);
      trainSheet.getCustomReadListenerList().add(listener);
      excelReader.read(trainSheet);
      result.setTrainFile(generatedFile);
      result.setTrainFileSize(listener.getLength());
    } catch (IOException e) {
      throw new BusinessIllegalStateException("解析训练集失败", e);
    }
    if (result.getTrainFileSize() == 0) {
      throw new BusinessIllegalStateException("解析训练集失败，训练集内容为空！");
    }
  }

  /**
   * 从excel中解析未标注集，校验并且重新写入一个json文件中
   *
   * @param excelReader  excel读取器
   * @param unlabelSheet 未标注集sheet
   * @param fileTask     文件任务
   * @param result       校验结果
   */
  private void parseUnlabelFromExcel(
      ExcelReader excelReader, ReadSheet unlabelSheet, FileTask fileTask, PreCheckResult result) {
    unlabelSheet.setClazz(TextUnlabelModel.class);
    // 下载的文件
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File generatedFile = new File(file.getParentFile(), "unlabeled_data.json");
    generatedFile.deleteOnExit();
    try (BufferedWriter writer =
        Files.newBufferedWriter(generatedFile.toPath(), StandardCharsets.UTF_8)) {
      UnlabelSheetListener listener = new UnlabelSheetListener(writer, objectMapper);
      unlabelSheet.getCustomReadListenerList().add(listener);
      excelReader.read(unlabelSheet);
      result.setUnlabelFileSize(listener.getLength());
      result.setUnlabelFile(generatedFile);
    } catch (IOException e) {
      throw new BusinessIllegalStateException("解析未标注集失败", e);
    }
    if (result.getUnlabelFileSize() == 0) {
      throw new BusinessIllegalStateException("解析未标注集失败，未标注集内容为空！");
    }
  }

  /**
   * 从excel中解析标签集，标签集被认为不会很多，并且需要缓存在内存中，所以直接全部读取
   *
   * @param excelReader excel读取器
   * @param labelSheet  标签集sheet
   * @param fileTask    文件任务
   * @param result      校验结果
   */
  private void parseLabelFromExcel(
      ExcelReader excelReader, ReadSheet labelSheet, FileTask fileTask, PreCheckResult result) {
    labelSheet.setClazz(TextLabelModel.class);
    LabelSheetReadListener readListener = new LabelSheetReadListener();
    labelSheet.getCustomReadListenerList().add(readListener);
    excelReader.read(labelSheet);
    List<TextLabelModel> labelList = readListener.getLabelList();
    validAndCache(labelList);
    // 重新写入标签集文件，生成单独的json文件？不生成好像也没啥关系
  }

  private void validAndCache(List<TextLabelModel> labelList) {
    if (labelList.size() == 0) {
      throw new BusinessIllegalStateException("标签集内容为空！");
    }
    for (TextLabelModel textLabelModel : labelList) {
      // 使用正则表达式判断是否包含两个及以上的汉字
      Matcher matcher = pattern.matcher(textLabelModel.getLabelDesc());
      int count = 0;
      while (matcher.find()) {
        count++;
      }
      if (count < 2) {
        throw new BusinessIllegalStateException("标签集内容中的标签描述不能少于两个汉字！");
      }
    }
    Map<Integer, String> labelMap =
        labelList.stream()
            .collect(Collectors.toMap(TextLabelModel::getLabelId, TextLabelModel::getLabelDesc));
    NEW_LABEL_HOLDER.set(labelMap);
    // 校验标签集和原有标签集是否不一致
    if (OLD_LABEL_HOLDER.get() != null) {
      validLabel();
    }
    Map<String, Integer> labelIdMap =
        labelList.stream()
            .collect(Collectors.toMap(TextLabelModel::getLabelDesc, TextLabelModel::getLabelId));
    LabelSupport.setCurrentLabel(labelIdMap);
  }

  private PreCheckResult parseZip(FileTask fileTask, File file) {
    File fileDir = new File(file.getParent(), FileUtil.mainName(file));
    File unzip = unZipFile(file, fileDir);
    List<File> files =
        FileUtil.loopFiles(
            unzip,
            pathname -> !pathname.isDirectory() && !pathname.getParent().contains("__MACOSX"));
    return checkFiles(files, fileTask);
  }

  private PreCheckResult checkFiles(List<File> files, FileTask fileTask) {
    if (files == null || files.size() == 0) {
      throw new BusinessIllegalStateException("获取压缩文件失败，压缩文件内没有符合规则名称的文件");
    }
    PreCheckResult result = new PreCheckResult();
    for (File file : files) {
      if (file.getName().contains(TRAIN_DATA_NAME)) {
        if (isNotJson(file)) {
          throw new BusinessIllegalStateException("训练集文件不是json格式");
        }
        result.setHasTrainFile(true);
        result.setTrainFile(file);
      } else if (file.getName().contains(UN_LABEL_DATA_NAME)) {
        if (isNotJson(file)) {
          throw new BusinessIllegalStateException("未标注集文件不是json格式");
        }
        result.setHasUnlabelFile(true);
        result.setUnlabelFile(file);
      } else if (file.getName().contains(LABEL_DATA_NAME)) {
        if (isNotJson(file)) {
          throw new BusinessIllegalStateException("标注集文件不是json格式");
        }
        result.setHasLabelFile(true);
        result.setLabelFile(file);
      } else if (file.getName().contains(TEST_DATA_NAME)) {
        if (isNotJson(file)) {
          throw new BusinessIllegalStateException("测试集文件不是json格式");
        }
        result.setHasTestFile(true);
        result.setTestFile(file);
      }
    }
    // 检查必传文件
    if (!result.isHasUnlabelFile() || !result.isHasTestFile() || !result.isHasLabelFile()) {
      throw new BusinessIllegalStateException("测试集文件，标签集文件，未标签集文件为必传文件");
    }
    // 先解析标签集文件
    parseLabelFile(result, fileTask);
    // 解析未标注集文件
    parseUnlabelFile(result, fileTask);
    // 如果有训练集文件，则解析训练集文件
    if (result.isHasTrainFile()) {
      parseTrainFile(result, fileTask);
    }
    // 解析测试集，如果训练集不存在，则需要从测试集拆分
    parseTestFile(result, fileTask);
    return result;
  }

  private boolean isNotJson(File file) {
    return !"json".equals(FileUtil.extName(file));
  }

  private void parseLabelFile(PreCheckResult result, FileTask fileTask) {
    File labelFile = result.getLabelFile();
    try (BufferedReader reader = Files.newBufferedReader(labelFile.toPath())) {
      String line;
      long lineNum = 1;
      List<TextLabelModel> labelList = new ArrayList<>();
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        TextLabelModel labelModel = objectMapper.readValue(line, TextLabelModel.class);
        if (StringUtils.isBlank(labelModel.getLabelDesc())) {
          throw new BusinessArgumentException("标签集第" + lineNum + "格式错误，标签描述为空！");
        }
        if (labelModel.getLabelId() == null) {
          throw new BusinessArgumentException("标签集第" + lineNum + "格式错误，标签ID为空！");
        }
        labelList.add(labelModel);
        lineNum++;
      }
      validAndCache(labelList);
    } catch (IOException e) {
      throw new BusinessIllegalStateException("标签集文件格式错误", e);
    }
  }

  private void parseUnlabelFile(PreCheckResult result, FileTask fileTask) {
    File unlabelFile = result.getUnlabelFile();
    // 重新生成的未标注集文件，因为解压出来的未标注集文件在文件夹内，所以不用考虑文件名重复
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File generatedFile = new File(file.getParentFile(), "unlabeled_data.json");
    try (BufferedReader reader = Files.newBufferedReader(unlabelFile.toPath());
        BufferedWriter writer = Files.newBufferedWriter(generatedFile.toPath())) {
      String line;
      long lineNum = 1;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        TextUnlabelModel model = objectMapper.readValue(line, TextUnlabelModel.class);
        if (StringUtils.isBlank(model.getSentence())) {
          throw new BusinessArgumentException("未标注集第" + lineNum + "格式错误，问法语料为空！");
        }
        writer.write(objectMapper.writeValueAsString(model));
        writer.newLine();
        lineNum++;
      }
      result.setUnlabelFile(generatedFile);
      result.setUnlabelFileSize(lineNum - 1);
    } catch (IOException e) {
      throw new BusinessIllegalStateException("未标注集文件格式错误", e);
    }
  }

  private void parseTrainFile(PreCheckResult result, FileTask fileTask) {
    File trainFile = result.getTrainFile();
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File generatedFile = new File(file.getParentFile(), "traindata.json");
    Map<String, Integer> currentLabel = LabelSupport.getCurrentLabel();
    try (BufferedReader reader = Files.newBufferedReader(trainFile.toPath());
        BufferedWriter writer = Files.newBufferedWriter(generatedFile.toPath())) {
      String line;
      long lineNum = 1;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        TextLabelDataModel model = objectMapper.readValue(line, TextLabelDataModel.class);
        if (StringUtils.isBlank(model.getSentence())) {
          throw new BusinessArgumentException("训练集第" + lineNum + "格式错误，问法语料为空！");
        }
        if (model.getLabelDes() != null) {
          if (!currentLabel.containsKey(model.getLabelDes())) {
            model.setLabel(null);
            model.setLabelDes(null);
          } else {
            model.setLabel(currentLabel.get(model.getLabelDes()));
          }
        }
        // 重新写入文件，去除一些不必要的字段
        writer.write(objectMapper.writeValueAsString(model));
        writer.newLine();
        lineNum++;
      }
      result.setTrainFile(generatedFile);
      result.setTrainFileSize(lineNum - 1);
    } catch (IOException e) {
      throw new BusinessIllegalStateException("训练集文件格式错误", e);
    }
  }

  /**
   * 解析测试集文件，如果训练集不存在，则需要从测试集拆分。切分策略为根据标签等分切分。切分分配优先级，测试集 > 训练集> 验证集。
   *
   * <p>测试集本身需要切分为验证集和测试集，即可见（验证集）和不可见（测试集）部分。
   *
   * @param result   检查结果
   * @param fileTask 文件任务
   */
  private void parseTestFile(PreCheckResult result, FileTask fileTask) {
    File file = Paths.get(fileTask.getLocalPath()).toFile();
    File testFile = new File(file.getParentFile(), "testdata.json");
    File valFile = new File(file.getParentFile(), "valdata.json");
    Map<String, Integer> currentLabel = LabelSupport.getCurrentLabel();
    Map<String, Integer> stateMap = new HashMap<>(currentLabel.size());
    for (String labelDes : currentLabel.keySet()) {
      stateMap.put(labelDes, 0);
    }
    try (BufferedReader reader = Files.newBufferedReader(result.getTestFile().toPath());
        BufferedWriter testWriter = Files.newBufferedWriter(testFile.toPath());
        BufferedWriter valWriter = Files.newBufferedWriter(valFile.toPath())) {
      BufferedWriter trainWriter = null;
      try {
        File trainFile = null;
        if (!result.isHasTrainFile()) {
          trainFile = new File(file.getParentFile(), "traindata.json");
          trainWriter = Files.newBufferedWriter(trainFile.toPath());
        }
        String line;
        long lineNum = 1;
        long testLength = 0;
        long valLength = 0;
        long trainLength = 0;
        while ((line = reader.readLine()) != null) {
          if (line.isBlank()) {
            continue;
          }
          TextLabelDataModel model = objectMapper.readValue(line, TextLabelDataModel.class);
          if (StringUtils.isBlank(model.getSentence())) {
            throw new BusinessArgumentException("测试集第" + lineNum + "格式错误，问法语料为空！");
          }
          if (StringUtils.isNotBlank(model.getLabelDes())) {
            if (!currentLabel.containsKey(model.getLabelDes())) {
              throw new BusinessArgumentException("测试集第" + lineNum + "格式错误，出现不存在标签集中的标签！");
            } else {
              model.setLabel(currentLabel.get(model.getLabelDes()));
            }
          } else {
            throw new BusinessArgumentException("测试集第" + lineNum + "格式错误，标签描述为空！");
          }
          Integer integer = stateMap.get(model.getLabelDes());
          if (integer == 0) {
            // 测试集
            model.setId(testLength);
            testWriter.write(objectMapper.writeValueAsString(model));
            testWriter.newLine();
            testLength++;
          } else if (integer == 1) {
            // 训练集
            model.setId(trainLength);
            trainWriter.write(objectMapper.writeValueAsString(model));
            trainWriter.newLine();
            trainLength++;
          } else {
            // 验证集
            model.setId(valLength);
            valWriter.write(objectMapper.writeValueAsString(model));
            valWriter.newLine();
            valLength++;
          }
          if (result.isHasTrainFile()) {
            // 如果存在，则只需要拆分两份，序号在0和2之间跳转
            stateMap.put(model.getLabelDes(), (integer + 2) % 3 == 1 ? 0 : (integer + 2) % 3);
          } else {
            // 如果不存在，则需要拆分三份，序号在0、1、2之间跳转
            stateMap.put(model.getLabelDes(), (integer + 1) % 3);
          }
          lineNum++;
        }
        result.setTestFile(testFile);
        result.setTestFileSize(testLength);
        if (trainFile != null) {
          result.setTrainFile(trainFile);
          result.setTrainFileSize(trainLength);
        }
        result.setValFile(valFile);
        result.setValFileSize(valLength);
      } finally {
        if (trainWriter != null) {
          trainWriter.close();
        }
      }
    } catch (IOException e) {
      throw new BusinessIllegalStateException("测试集文件格式错误", e);
    }
  }

  @Override
  protected void finallyOp(FileTask fileTask) {
    super.finallyOp(fileTask);
    IDS_HOLDER.remove();
    OLD_LABEL_HOLDER.remove();
    NEW_LABEL_HOLDER.remove();
    LabelSupport.removeCurrentLabel();
  }

  @Override
  protected void handleTaskSuccess(String requestId, FileTask fileTask) {
    super.handleTaskSuccess(requestId, fileTask);
  }

  @Override
  protected void handleTaskError(String requestId, FileTask fileTask, String errorMsg) {
    super.handleTaskError(requestId, fileTask, errorMsg);
    // 删除完oss上的文件后，没有其他文件，则删除目录
    // 通过websocket通知客户端任务失败，并告知失败原因
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.DATASET_PARSE_FAIL);
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(
            infoPO.getId(),
            infoPO.getName(),
            String.format("任务：%s，上传的数据集文件解析失败，失败原因为%s", infoPO.getName(), errorMsg),
            false));
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  public void afterHandle(FileTask fileTask) {
    // 发送文件解析成功事件，触发算法高频词汇提取任务
    FileTask task = new FileTask();
    task.setTaskId(fileTask.getTaskId());
    task.setType(HF_WORD);
    FileTaskAppendEvent event = new FileTaskAppendEvent(task);
    eventPublisher.publishEvent(event);
    // 通过websocket通知客户端任务完成
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.DATASET_PARSE_SUCCESS);
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(
            infoPO.getId(),
            infoPO.getName(),
            String.format("任务：%s，上传的数据集文件解析成功", infoPO.getName()),
            true));
    WebSocketHandler.sendByUid(uid, dto);
    // 重新触发规则任务
    retrigger(fileTask.getTaskId(), uid, dto);
  }

  private void retrigger(Long taskId, String uid, WebsocketDTO dto) {
    List<RuleInfoPO> byTaskId = ruleInfoService.getRuleListByTaskId(taskId);
    if (!byTaskId.isEmpty()) {
      RuleTask ruleTask = new RuleTask();
      ruleTask.setTaskId(taskId);
      ruleTask.setType(RuleTaskType.BATCH);
      ruleTask.setRuleInfoPOList(byTaskId);
      Map<String, Object> config = new HashMap<>();
      config.put(SESSION_UID, uid);
      ruleTask.getConfig().setParams(config);
      RuleTaskAppendEvent event = new RuleTaskAppendEvent(ruleTask);
      publisher.publish(event);
      dto.setEvent(WsEventType.RULE_SUCCESS);
      dto.getData().setMsg("重新运行所有规则任务！");
      WebSocketHandler.sendByUid(uid, dto);
    }
  }

  @Override
  protected String getTaskCacheKey(FileTask fileTask) {
    return RedisKeyMethods.generateDatasetKey(fileTask.getTaskId());
  }

  private void validLabel() {
    Map<Integer, String> oldLabel = OLD_LABEL_HOLDER.get();
    Map<Integer, String> newLabel = NEW_LABEL_HOLDER.get();
    // 遍历原有标签，查看是否有标签删减，并且原有的标签和新标签是否不一致
    for (Entry<Integer, String> entry : oldLabel.entrySet()) {
      if (!newLabel.containsKey(entry.getKey())) {
        throw new BusinessIllegalStateException("标签集内容与原有标签不一致，请检查标签或者重新创建任务！");
      } else {
        if (!entry.getValue().equals(newLabel.get(entry.getKey()))) {
          throw new BusinessIllegalStateException("标签集内容与原有标签不一致，请检查标签或者重新创建任务！");
        }
      }
    }
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Autowired
  public void setInternalHandle(DatasetParseInternal internalHandle) {
    this.internalHandle = internalHandle;
  }
}
