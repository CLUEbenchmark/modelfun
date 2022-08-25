package com.wl.xc.modelfun.tasks.rule.handlers;

import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0
 * @date 2022/4/28 10:56
 */
public abstract class AbstractOutLabelHandler implements RuleTaskHandler {

  protected Long taskId;

  protected LabelInfoService labelInfoService;

  protected Map<String, Integer> labelMap;

  protected Set<Integer> labelIds;

  public AbstractOutLabelHandler(Long taskId) {
    this.taskId = taskId;
  }

  @Override
  public void init() {
    List<LabelInfoPO> infoPOS = labelInfoService.selectListByTaskId(taskId);
    labelMap = new HashMap<>(infoPOS.size());
    labelIds = new HashSet<>(infoPOS.size());
    for (LabelInfoPO infoPO : infoPOS) {
      labelMap.put(infoPO.getLabelDesc(), infoPO.getLabelId());
      labelIds.add(infoPO.getLabelId());
    }
  }

  protected int getSystemLabelId(String labelDescOrId) {
    // 先判断是不是文本，如果是文本，则获取文本对应的id
    Integer labelId = labelMap.get(labelDescOrId);
    if (labelId == null) {
      // 如果上一步没有获取到ID，判断是不是数字
      if (StringUtils.isNumeric(labelDescOrId)) {
        // 如果是数字，则去系统标签中查询是不是存在该ID
        labelId = Integer.parseInt(labelDescOrId);
        if (!labelIds.contains(labelId)) {
          labelId = -1;
        }
      } else {
        labelId = -1;
      }
    }
    return labelId;
  }

  protected Integer getLabel(Integer outerLabel) {
    if (outerLabel == null) {
      return -1;
    }
    return labelIds.contains(outerLabel) ? outerLabel : -1;
  }

  @Override
  public void afterLabel() {

  }

  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Override
  public void destroy() {
    if (labelMap != null) {
      labelIds.clear();
      labelIds = null;
    }
    if (labelMap != null) {
      labelMap.clear();
      labelMap = null;
    }
  }
}
