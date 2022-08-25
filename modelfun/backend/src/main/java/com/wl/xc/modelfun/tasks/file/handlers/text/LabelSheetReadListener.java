package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * 从Excel中读取标签集的监听器
 *
 * @version 1.0
 * @date 2022/6/17 13:43
 */
public class LabelSheetReadListener implements ReadListener<TextLabelModel> {

  private final List<TextLabelModel> labelList = new ArrayList<>(20);

  @Override
  public void invoke(TextLabelModel data, AnalysisContext context) {
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    if (data.getLabelId() == null) {
      throw new BusinessArgumentException("第" + rowIndex + "行数据有误,标签id不能为空");
    }
    if (StringUtils.isBlank(data.getLabelDesc())) {
      throw new BusinessArgumentException("第" + rowIndex + "行数据有误,标签名称不能为空");
    }
    labelList.add(data);
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
  }

  @Override
  public void onException(Exception exception, AnalysisContext context) throws Exception {
    int rowIndex = context.readRowHolder().getRowIndex() + 1;
    if (exception instanceof BusinessException) {
      throw exception;
    }
    throw new BusinessArgumentException("第" + rowIndex + "行数据有误，请检查！", exception);
  }

  public List<TextLabelModel> getLabelList() {
    return labelList;
  }
}
