package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.tasks.file.handlers.LabelSupport;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0
 * @date 2022/6/17 14:13
 */
public class TrainDataSheetListener implements ReadListener<TextLabelDataModel> {

  private final List<TextLabelDataModel> dataModelList = new ArrayList<>(100);

  private final BufferedWriter writer;

  private final ObjectMapper objectMapper;

  private long length = 0;

  public TrainDataSheetListener(BufferedWriter writer, ObjectMapper objectMapper) {
    this.writer = writer;
    this.objectMapper = objectMapper;
  }

  @Override
  public void onException(Exception exception, AnalysisContext context) throws Exception {
    if (exception instanceof BusinessException) {
      throw exception;
    }
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    throw new BusinessArgumentException("训练集第" + rowIndex + "行数据有误，请检查！", exception);
  }

  @Override
  public void invoke(TextLabelDataModel data, AnalysisContext context) {
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    if (StringUtils.isBlank(data.getSentence())) {
      throw new BusinessArgumentException("第" + rowIndex + "行数据有误,训练集问法语料不能为空");
    }
    Map<String, Integer> currentLabel = LabelSupport.getCurrentLabel();
    if (!currentLabel.containsKey(data.getLabelDes())) {
      data.setLabelDes(null);
      data.setLabel(null);
    } else {
      data.setLabel(currentLabel.get(data.getLabelDes()));
    }
    dataModelList.add(data);
    if (dataModelList.size() >= 100) {
      try {
        for (TextLabelDataModel textLabelDataModel : dataModelList) {
          writer.write(objectMapper.writeValueAsString(textLabelDataModel));
          writer.newLine();
          length++;
        }
        dataModelList.clear();
      } catch (Exception e) {
        throw new BusinessArgumentException("服务内部错误", e);
      }
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    if (!dataModelList.isEmpty()) {
      try {
        for (TextLabelDataModel textLabelDataModel : dataModelList) {
          length++;
          writer.write(objectMapper.writeValueAsString(textLabelDataModel));
          writer.newLine();
        }
        dataModelList.clear();
      } catch (Exception e) {
        throw new BusinessArgumentException("服务内部错误", e);
      }
    }
  }

  public long getLength() {
    return length;
  }
}
