package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0
 * @date 2022/6/17 14:13
 */
public class UnlabelSheetListener implements ReadListener<TextUnlabelModel> {

  private final List<TextUnlabelModel> unlabelList = new ArrayList<>(100);

  private final BufferedWriter writer;

  private final ObjectMapper objectMapper;

  private long length = 0;

  public UnlabelSheetListener(BufferedWriter writer, ObjectMapper objectMapper) {
    this.writer = writer;
    this.objectMapper = objectMapper;
  }

  @Override
  public void onException(Exception exception, AnalysisContext context) throws Exception {
    if (exception instanceof BusinessException) {
      throw exception;
    }
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    throw new BusinessArgumentException("第" + rowIndex + "行数据有误，请检查！", exception);
  }

  @Override
  public void invoke(TextUnlabelModel data, AnalysisContext context) {
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    if (StringUtils.isBlank(data.getSentence())) {
      throw new BusinessArgumentException("第" + rowIndex + "行数据有误,未标注集问法语料不能为空");
    }
    unlabelList.add(data);
    if (unlabelList.size() >= 100) {
      try {
        for (TextUnlabelModel textUnlabelModel : unlabelList) {
          writer.write(objectMapper.writeValueAsString(textUnlabelModel));
          writer.newLine();
          length++;
        }
        unlabelList.clear();
      } catch (Exception e) {
        throw new BusinessArgumentException("服务内部错误", e);
      }
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    if (!unlabelList.isEmpty()) {
      try {
        for (TextUnlabelModel textUnlabelModel : unlabelList) {
          length++;
          writer.write(objectMapper.writeValueAsString(textUnlabelModel));
          writer.newLine();
        }
        unlabelList.clear();
      } catch (Exception e) {
        throw new BusinessArgumentException("服务内部错误", e);
      }
    }
  }

  public long getLength() {
    return length;
  }
}
