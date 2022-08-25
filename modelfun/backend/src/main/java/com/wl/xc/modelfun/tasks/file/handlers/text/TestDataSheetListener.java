package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.tasks.file.handlers.LabelSupport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0
 * @date 2022/6/17 15:45
 */
public class TestDataSheetListener implements ReadListener<TextLabelDataModel> {

  private final List<TextLabelDataModel> dataModelList = new ArrayList<>(100);

  /**
   * 测试集的文件写入流
   */
  private final BufferedWriter testFileWriter;
  /**
   * 验证集的文件写入流，即可见部分
   */
  private final BufferedWriter valFileWriter;
  /**
   * 训练集的文件写入流，可以为空
   */
  private BufferedWriter trainFileWriter;

  private final ObjectMapper objectMapper;

  private long testLength = 0;

  private long valLength = 0;

  private long trainLength = 0;

  private boolean hasTrainFile = true;

  private final Map<String, Integer> stage;
  /**
   * 三种状态，分别是测试集、验证集、训练集，表示当前标签对应的语料应该写入哪个文件,切分分配优先级，测试集 > 训练集> 验证集
   */
  private final Integer testStage = 0;
  private final Integer trainStage = 1;
  private final Integer valStage = 2;

  private final Map<Integer, Consumer<TextLabelDataModel>> stageConsumerMap = new HashMap<>();

  public TestDataSheetListener(
      BufferedWriter testFileWriter, BufferedWriter valFileWriter, ObjectMapper objectMapper) {
    this.testFileWriter = testFileWriter;
    this.valFileWriter = valFileWriter;
    this.objectMapper = objectMapper;
    Map<String, Integer> currentLabel = LabelSupport.getCurrentLabel();
    stage = new HashMap<>(currentLabel.size());
    for (Map.Entry<String, Integer> entry : currentLabel.entrySet()) {
      stage.put(entry.getKey(), testStage);
    }
    stageConsumerMap.put(testStage, this::writeTestFile);
    stageConsumerMap.put(valStage, this::writeValFile);
    stageConsumerMap.put(trainStage, this::writeTrainFile);
  }


  @Override
  public void onException(Exception exception, AnalysisContext context) throws Exception {
    if (exception instanceof BusinessException) {
      throw exception;
    }
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    throw new BusinessArgumentException("测试集第" + rowIndex + "行数据有误，请检查！", exception);
  }

  @Override
  public void invoke(TextLabelDataModel data, AnalysisContext context) {
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    if (StringUtils.isBlank(data.getSentence())) {
      throw new BusinessArgumentException("第" + rowIndex + "行数据有误,测试集问法语料不能为空");
    }
    if (StringUtils.isBlank(data.getLabelDes())) {
      throw new BusinessArgumentException("第" + rowIndex + "行数据有误,测试集标签名称不能为空");
    }
    Map<String, Integer> currentLabel = LabelSupport.getCurrentLabel();
    // 如果有不存在的标签，直接忽略这句话
    if (!currentLabel.containsKey(data.getLabelDes())) {
      return;
    }
    data.setLabel(currentLabel.get(data.getLabelDes()));
    dataModelList.add(data);
    if (dataModelList.size() >= 100) {
      writeData(dataModelList);
      dataModelList.clear();
    }
  }

  private void writeData(List<TextLabelDataModel> dataModelList) {
    for (TextLabelDataModel data : dataModelList) {
      if (hasTrainFile) {
        // 如果存在训练集文件，就不需要拆分3份
        Integer integer = stage.get(data.getLabelDes());
        stageConsumerMap.get(integer).accept(data);
        stage.put(data.getLabelDes(), (integer + 2) % 3 == 1 ? testStage : (integer + 2) % 3);
      } else {
        // 如果不存在训练集文件，就拆分3份
        Integer integer = stage.get(data.getLabelDes());
        stageConsumerMap.get(integer).accept(data);
        stage.put(data.getLabelDes(), (integer + 1) % 3);
      }
    }
  }

  private void writeTestFile(TextLabelDataModel textLabelDataModel) {
    try {
      testFileWriter.write(objectMapper.writeValueAsString(textLabelDataModel));
      testFileWriter.newLine();
      testLength++;
    } catch (IOException e) {
      throw new BusinessIllegalStateException("服务内部错误", e);
    }
  }

  private void writeValFile(TextLabelDataModel textLabelDataModel) {
    try {
      valFileWriter.write(objectMapper.writeValueAsString(textLabelDataModel));
      valFileWriter.newLine();
      valLength++;
    } catch (IOException e) {
      throw new BusinessIllegalStateException("服务内部错误", e);
    }
  }

  private void writeTrainFile(TextLabelDataModel textLabelDataModel) {
    try {
      trainFileWriter.write(objectMapper.writeValueAsString(textLabelDataModel));
      trainFileWriter.newLine();
      trainLength++;
    } catch (IOException e) {
      throw new BusinessIllegalStateException("服务内部错误", e);
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    if (!dataModelList.isEmpty()) {
      writeData(dataModelList);
      dataModelList.clear();
    }
  }

  public void setTrainFileWriter(BufferedWriter trainFileWriter) {
    this.trainFileWriter = trainFileWriter;
  }

  public void setHasTrainFile(boolean hasTrainFile) {
    this.hasTrainFile = hasTrainFile;
  }

  public long getTestLength() {
    return testLength;
  }

  public long getValLength() {
    return valLength;
  }

  public long getTrainLength() {
    return trainLength;
  }
}
