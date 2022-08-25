package com.wl.xc.modelfun.tasks.rule.handlers;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.ExpertRule;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 专家知识规则处理器
 *
 * @version 1.0
 * @date 2022/4/15 15:40
 */
public class ExpertHandler extends AbstractOutLabelHandler {

  private final Pattern pattern = Pattern.compile("([^\\t]+)\\t+([^\\t]+)");

  private final List<ExpertRule> expertRules;

  private OssService ossService;

  private List<Map<String, Integer>> expertList = new ArrayList<>(2000);

  public ExpertHandler(List<ExpertRule> expertRules, Long taskId) {
    super(taskId);
    this.expertRules = expertRules;
    this.taskId = taskId;
  }

  @Override
  public RuleType getRuleType() {
    return null;
  }

  @Override
  public void init() {
    try {
      super.init();
      List<String> list = expertRules.stream().map(ExpertRule::getAddress).collect(Collectors.toList());
      for (String fileName : list) {
        HashMap<String, Integer> map = new HashMap<>(2000);
        ossService.downloadStream(fileName, line -> readLine(line, map));
        expertList.add(map);
      }
    } catch (Exception e) {
      throw new BusinessIllegalStateException("解析专家知识失败！", e);
    }

  }

  private void readLine(String line, Map<String, Integer> map) {
    while (line.endsWith("\t")) {
      line = line.substring(0, line.length() - 1);
    }
    Matcher matcher = pattern.matcher(line);
    if (matcher.matches()) {
      String sentence = matcher.group(1);
      String labelId = matcher.group(2);
      map.put(sentence, getSystemLabelId(labelId));
    }
  }

  @Override
  public int label(String sentence, DatasetType datasetType) {
    for (Map<String, Integer> map : expertList) {
      for (String s : map.keySet()) {
        if (sentence.contains(s)) {
          return map.get(s);
        }
      }
    }
    return -1;
  }

  @Override
  public void destroy() {
    expertList.forEach(Map::clear);
    expertList.clear();
    expertList = null;
    super.destroy();
  }

  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }
}
