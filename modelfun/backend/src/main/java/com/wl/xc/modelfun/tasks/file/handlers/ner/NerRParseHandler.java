package com.wl.xc.modelfun.tasks.file.handlers.ner;

import static com.wl.xc.modelfun.commons.FileConstant.NER_LABEL_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TEST_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TRAIN_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.UN_LABEL_DATA_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.OneClickEntityDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.NerLabelModel;
import com.wl.xc.modelfun.entities.model.NerTestDataModel;
import com.wl.xc.modelfun.entities.model.NerTestDataModel.EntitiesDTO;
import com.wl.xc.modelfun.entities.model.NerUnLabel;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.handlers.AbstractFileUploadTaskHandler;
import com.wl.xc.modelfun.tasks.file.handlers.LabelSupport;
import com.wl.xc.modelfun.tasks.file.handlers.PreCheckResult;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/6/7 10:00
 */
@Slf4j
@Component
public class NerRParseHandler extends AbstractFileUploadTaskHandler {

  private NerRInternalHandle nerRInternalHandle;

  private final Map<Integer, Function<String, NerTestDataModel>> TEST_DATA_MAP = new HashMap<>();

  public NerRParseHandler() {
    TEST_DATA_MAP.put(1, this::parseTestData);
    TEST_DATA_MAP.put(2, this::parseTestData2);
    TEST_DATA_MAP.put(3, this::parseTestData);
  }

  @Override
  public FileTaskType getType() {
    return FileTaskType.NER_R;
  }

  @Override
  public void afterHandle(FileTask fileTask) {
    // 通过websocket通知客户端任务完成
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(
            infoPO.getId(),
            infoPO.getName(),
            String.format("任务：%s，上传的NER数据集文件解析成功", infoPO.getName()),
            true));
    dto.setEvent(WsEventType.DATASET_PARSE_SUCCESS);
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected void internalHandle(FileTask fileTask) {
    List<File> files = getLocalFiles(fileTask.getLocalPath());
    PreCheckResult result = checkFiles(files, fileTask);
    fileTask.getConfig().put("PreCheckResult", result);
    nerRInternalHandle.internalHandle(fileTask);
  }

  @Override
  protected void handleTaskSuccess(String requestId, FileTask fileTask) {
    stringRedisTemplate.delete(getTaskCacheKey(fileTask));
  }

  @Override
  protected void handleTaskError(String requestId, FileTask fileTask, String errorMsg) {
    stringRedisTemplate.delete(getTaskCacheKey(fileTask));
    // 删除oss文件
    String ossParent = fileUploadProperties.getOssPrefix() + fileTask.getTaskId() + "/dataset/";
    List<String> dirFiles = ossService.listDirFiles(ossParent);
    ossService.deleteFiles(dirFiles);
    // 删除完oss上的文件后，没有其他文件，则删除目录
    // 通过websocket通知客户端任务失败，并告知失败原因
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(infoPO.getId(), infoPO.getName(),
            String.format("任务：%s，上传NER数据集文件解析失败，失败原因为%s", infoPO.getName(), errorMsg),
            false));
    dto.setEvent(WsEventType.DATASET_PARSE_FAIL);
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected void finallyOp(FileTask fileTask) {
    LabelSupport.removeCurrentLabel();
  }

  private PreCheckResult checkFiles(List<File> files, FileTask fileTask) {
    if (files == null || files.isEmpty()) {
      throw new BusinessIllegalStateException("压缩包内没有文件，请重新上传数据集文件");
    }
    PreCheckResult result = new PreCheckResult();
    for (File file : files) {
      if (file.getName().contains(TRAIN_DATA_NAME)) {
        result.setHasTrainFile(true);
        result.setTrainFile(file);
      } else if (file.getName().contains(TEST_DATA_NAME)) {
        result.setHasTestFile(true);
        result.setTestFile(file);
      } else if (file.getName().contains(UN_LABEL_DATA_NAME)) {
        result.setHasUnlabelFile(true);
        result.setUnlabelFile(file);
      } else if (file.getName().contains(NER_LABEL_NAME)) {
        result.setHasLabelFile(true);
        result.setLabelFile(file);
      }
    }
    if (!result.isHasUnlabelFile() || !result.isHasLabelFile()) {
      throw new BusinessIllegalStateException("未标注集文件和标签集文件为必传文件，请检查数据集文件");
    }
    // 解析标签集文件
    parseLabelFile(result.getLabelFile(), result);
    Integer fileType = (Integer) fileTask.getConfig().get("fileType");
    if (result.isHasTestFile()) {
      // 如果存在测试集文件，则解析测试集文件
      parseTestFile(result, fileType);
    }
    if (result.isHasTrainFile()) {
      // 如果存在训练集文件，则解析训练集文件
      parseTrainFile(result, fileType);
    }
    // 解析未标注集文件
    parseUnlabelFile(result);
    return result;
  }

  /**
   * <pre>
   * 解析未标注集文件。
   * 1. 如果用户没有上传测试集文件，那么未标注文件需要切割一部分数据作为测试集
   * 2. 如果用户没有上传训练集文件，那么未标注文件需要切割一部分数据作为训练集
   * 切割的原则为：
   *  1. 测试集和训练集的数据量比例为1:1，各自为未标注集的10%（可配置）
   *  2. 每个标签需要切割20（可配置）条数据，如果10%的未标注集不足20 * 标签数量，则按照10%的未标注集数量切割，否则，按照标签数量切割
   * </pre>
   *
   * @param result 校验结果
   */
  private void parseUnlabelFile(PreCheckResult result) {
    File unlabelFile = result.getUnlabelFile();
    long count = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(unlabelFile))) {
      long lineCount = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        count++;
        if (line.isBlank()) {
          continue;
        }
        lineCount++;
        NerUnLabel parse = objectMapper.readValue(line, NerUnLabel.class);
        if (StringUtils.isBlank(parse.getText())) {
          throw new BusinessArgumentException("未标注数据语料内容不能为空！");
        }
      }
      result.setUnlabelFileSize(lineCount);
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException(
          "解析未标注集文件失败:" + "第" + count + "行数据格式错误！" + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("解析未标注集文件失败:" + "第" + count + "行数据格式错误！", e);
    }
    if (result.getUnlabelFileSize() == 0) {
      throw new BusinessIllegalStateException("未标注集文件内容为空，请检查数据集文件");
    }
    if (result.isHasTrainFile() && result.isHasTestFile()) {
      // 如果用户上传了测试集和训练集，则不需要切割未标注集
      return;
    }
    String maxSize = CalcUtil.multiply(String.valueOf(result.getUnlabelFileSize()), String.valueOf(0.4), 0, false);
    long max = Long.parseLong(maxSize);
    int labelSize = LabelSupport.getCurrentLabel().size();
    long totalDataCount = 0;
    // 如果用户没有上传测试集文件，那么未标注文件需要切割一部分数据作为测试集
    if (!result.isHasTestFile()) {
      totalDataCount = totalDataCount + (long) labelSize * 20;
    }
    // 如果用户没有上传训练集文件，那么未标注文件需要切割一部分数据作为测试集
    if (!result.isHasTrainFile()) {
      totalDataCount = totalDataCount + (long) labelSize * 20;
    }
    // 最终切割的数据量
    max = Math.min(max, totalDataCount);
    shardingFile(result, max);
  }

  private void shardingFile(PreCheckResult result, long max) {
    String multiply = CalcUtil.multiply(String.valueOf(max), String.valueOf(0.5), 0, false);
    long half = Long.parseLong(multiply);
    long total = half * 2;
    File unlabelFile = result.getUnlabelFile();
    long length = result.getUnlabelFileSize();
    File testFile = new File(unlabelFile.getParentFile(), "testdata.json");
    File trainFile = new File(unlabelFile.getParentFile(), "traindata.json");
    File gen = new File(unlabelFile.getParentFile(), FileUtil.mainName(unlabelFile) + "_generate.json");
    try (BufferedReader reader = new BufferedReader(new FileReader(unlabelFile));
        BufferedWriter testW = new BufferedWriter(new FileWriter(testFile));
        BufferedWriter genW = new BufferedWriter(new FileWriter(gen));
        BufferedWriter trainW = new BufferedWriter(new FileWriter(trainFile))) {
      String line;
      long count = 0;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        count++;
        // 如果没有测试集文件，有训练集文件
        if (!result.isHasTestFile() && result.isHasTrainFile()) {
          // 抽取出一部分数据作为测试集
          if (count <= half) {
            testW.write(line);
            if (count < half) {
              testW.newLine();
            }
          } else {
            genW.write(line);
            if (count < length) {
              genW.newLine();
            }
          }
        } else if (result.isHasTestFile() && !result.isHasTrainFile()) {
          // 如果有测试集文件，没有训练集文件
          if (count <= half) {
            trainW.write(line);
            if (count < half) {
              trainW.newLine();
            }
          } else {
            genW.write(line);
            if (count < length) {
              genW.newLine();
            }
          }
        } else if (!result.isHasTestFile() && !result.isHasTrainFile()) {
          // 如果测试集文件和训练集文件都没有
          if (count <= half) {
            testW.write(line);
            if (count < half) {
              testW.newLine();
            }
          } else if (count <= total) {
            trainW.write(line);
            if (count < total) {
              trainW.newLine();
            }
          } else {
            genW.write(line);
            if (count < length) {
              genW.newLine();
            }
          }
        }
      }
    } catch (Exception e) {
      throw new BusinessIllegalStateException("服务器内部错误！", e);
    }
    if (!result.isHasTestFile()) {
      result.setTestFile(testFile);
      result.setTestFileSize(half);
      length = length - half;
    }
    if (!result.isHasTrainFile()) {
      result.setTrainFile(trainFile);
      result.setTrainFileSize(half);
      length = length - half;
    }
    result.setUnlabelFile(gen);
    result.setUnlabelFileSize(length);
  }

  private void parseLabelFile(File labelFile, PreCheckResult result) {
    Map<String, Integer> labelMap = new HashMap<>();
    long count = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(labelFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        count++;
        if (line.isBlank()) {
          continue;
        }
        NerLabelModel model = objectMapper.readValue(line, NerLabelModel.class);
        if (!StringUtils.isNumeric(model.getNer())) {
          throw new BusinessArgumentException("标签ID不能为空且必须为数字！");
        }
        if (StringUtils.isBlank(model.getNerDes())) {
          throw new BusinessArgumentException("标签描述不能为空！");
        }
        if (labelMap.containsKey(model.getNer())) {
          throw new BusinessArgumentException("标签描述重复！");
        }
        int labelId = Integer.parseInt(model.getNer());
        if (labelMap.containsValue(labelId)) {
          throw new BusinessArgumentException("标签ID重复！");
        }
        labelMap.put(model.getNerDes(), labelId);
      }
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException(
          "解析标签集文件失败:" + "第" + count + "行数据格式错误！" + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("解析标签集文件失败:" + "第" + count + "行数据格式错误！", e);
    }
    if (labelMap.isEmpty()) {
      throw new BusinessIllegalStateException("标签集文件内容为空，请检查标签集文件!");
    }
    LabelSupport.setCurrentLabel(labelMap);
    result.setLabelFileSize(labelMap.size());
  }

  private void parseTestFile(PreCheckResult result, Integer fileType) {
    File testFile = result.getTestFile();
    File generatedFile =
        new File(testFile.getParentFile(), FileUtil.mainName(testFile) + "_generate.json");
    long lineCount = 0;
    int entityCount = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(testFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(generatedFile))) {
      String line;
      long count = 0;
      while ((line = reader.readLine()) != null) {
        lineCount++;
        if (line.isBlank()) {
          continue;
        }
        NerTestDataModel testData = TEST_DATA_MAP.get(fileType).apply(line);
        if (!testData.getEntities().isEmpty()) {
          for (EntitiesDTO entity : testData.getEntities()) {
            entity.setId(entityCount++);
          }
        }
        testData.setId(count);
        count++;
        writer.write(objectMapper.writeValueAsString(testData));
        writer.newLine();
      }
      result.setTestFile(generatedFile);
      result.setTestFileSize(count);
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException(
          "解析测试集文件失败:" + "第" + lineCount + "行数据格式错误！" + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("解析测试集文件失败:" + "第" + lineCount + "行数据格式错误！", e);
    }
  }

  private NerTestDataModel parseTestData(String line) {
    NerTestDataModel model;
    try {
      model = objectMapper.readValue(line, NerTestDataModel.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("数据格式错误！", e);
    }
    validLineData(model);
    validEntities(model);
    return model;
  }

  private NerTestDataModel parseTestData2(String line) {
    NerTestDataModel model;
    try {
      Map<String, Integer> map = LabelSupport.getCurrentLabel();
      OneClickEntityDTO oneClickEntityDTO = objectMapper.readValue(line, OneClickEntityDTO.class);
      model = new NerTestDataModel();
      model.setText(oneClickEntityDTO.getText());
      model.setRelations(Collections.emptyList());
      Map<String, Map<String, List<List<Integer>>>> labelMap = oneClickEntityDTO.getLabel();
      List<NerTestDataModel.EntitiesDTO> list = new ArrayList<>();
      for (Entry<String, Map<String, List<List<Integer>>>> entry : labelMap.entrySet()) {
        String labelName = entry.getKey();
        if (!map.containsKey(labelName)) {
          continue;
        }
        Map<String, List<List<Integer>>> entityMap = entry.getValue();
        for (Entry<String, List<List<Integer>>> entityEntry : entityMap.entrySet()) {
          List<List<Integer>> entityList = entityEntry.getValue();
          for (List<Integer> entity : entityList) {
            NerTestDataModel.EntitiesDTO dto = new NerTestDataModel.EntitiesDTO();
            dto.setLabel(labelName);
            dto.setStartOffset(entity.get(0));
            dto.setEndOffset(entity.get(1));
            list.add(dto);
          }
        }
      }
      model.setEntities(list);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("数据格式错误！", e);
    }
    validLineData(model);
    return model;
  }

  private void parseTrainFile(PreCheckResult result, Integer fileType) {
    File testFile = result.getTrainFile();
    File generatedFile =
        new File(testFile.getParentFile(), FileUtil.mainName(testFile) + "_generate.json");
    long lineCount = 0;
    int entityCount = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(testFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(generatedFile))) {
      String line;
      long count = 0;
      while ((line = reader.readLine()) != null) {
        lineCount++;
        if (line.isEmpty()) {
          continue;
        }
        count++;
        NerTestDataModel testData = TEST_DATA_MAP.get(fileType).apply(line);
        if (fileType == 2 && !testData.getEntities().isEmpty()) {
          for (EntitiesDTO entity : testData.getEntities()) {
            entity.setId(entityCount++);
          }
        }
        writer.write(objectMapper.writeValueAsString(testData));
        writer.newLine();
      }
      result.setTrainFile(generatedFile);
      result.setTrainFileSize(count);
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException(
          "解析训练集文件失败:" + "第" + lineCount + "行数据格式错误！" + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("解析训练集文件失败:" + "第" + lineCount + "行数据格式错误！", e);
    }
  }

  private void validLineData(NerTestDataModel model) {
    if (StringUtils.isBlank(model.getText())) {
      throw new BusinessArgumentException("语料不能为空！");
    }
    // 如果标签实体列表为空，那么也可以支持上传，但是需要用户自己打标签
    if (model.getEntities() == null || model.getEntities().isEmpty()) {
      model.setEntities(Collections.emptyList());
      return;
    }
    for (NerTestDataModel.EntitiesDTO entity : model.getEntities()) {
      if (entity.getStartOffset() == null || entity.getEndOffset() == null) {
        throw new BusinessArgumentException("实体起始偏移量和结束偏移量不能为空！");
      }
      if (StringUtils.isBlank(entity.getLabel())) {
        throw new BusinessArgumentException("实体标签不能为空！");
      }
    }
  }

  /**
   * 检查测试数据对象是否是有效的
   *
   * <p>有效的意义即：所包含的实体中，存在标签集内的实体，如果一个实体都没有，则认为该数据无效
   *
   * <p>该方法有副作用
   *
   * @param testData 测试数据对象
   */
  private void validEntities(NerTestDataModel testData) {
    List<EntitiesDTO> entities = testData.getEntities();
    Map<String, Integer> map = LabelSupport.getCurrentLabel();
    entities.removeIf(e -> !map.containsKey(e.getLabel()));
  }

  @Override
  protected String getTaskCacheKey(FileTask fileTask) {
    return RedisKeyMethods.generateDatasetKey(fileTask.getTaskId());
  }

  @Autowired
  public void setNerRInternalHandle(NerRInternalHandle nerRInternalHandle) {
    this.nerRInternalHandle = nerRInternalHandle;
  }
}
