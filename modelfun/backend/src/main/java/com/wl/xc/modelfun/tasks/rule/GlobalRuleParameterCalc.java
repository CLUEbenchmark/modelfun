package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.RuleOverviewPO;
import com.wl.xc.modelfun.entities.po.RuleUnlabeledResultPO;
import com.wl.xc.modelfun.entities.po.RuleVotePO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleOverviewService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.PageUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 规则的全局参数计算，主要包括规则的冲突率和规则的重叠率。以及标注概览的准确率、覆盖率和冲突率。
 *
 * @version 1.0
 * @date 2022.4.16 0:02
 */
@Slf4j
@Component
public class GlobalRuleParameterCalc {

  private RuleInfoService ruleInfoService;

  private RuleUnlabeledResultService ruleUnlabeledResultService;

  private RuleResultService ruleResultService;

  private RuleOverviewService ruleOverviewService;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  /**
   * 根据任务ID计算所有规则的冲突率和重叠率
   *
   * @param ruleTask 规则任务
   */
  @Transactional(rollbackFor = Exception.class)
  public void globalCalc(RuleTask ruleTask) {
    Long taskId = ruleTask.getTaskId();
    RuleInfoPO po = new RuleInfoPO();
    po.setTaskId(taskId);
    po.setCompleted(1);
    List<RuleInfoPO> ruleInfoPOS = ruleInfoService.selectBySelective(po);
    if (ruleInfoPOS == null || ruleInfoPOS.size() == 0) {
      RuleOverviewPO infoPO = new RuleOverviewPO();
      infoPO.setTaskId(taskId);
      infoPO.setConflict("0");
      infoPO.setCoverage("0");
      infoPO.setAccuracy("0");
      infoPO.setTestDataCoverage("0");
      infoPO.setUpdateDatetime(LocalDateTime.now());
      ruleOverviewService.insertRuleOverview(infoPO);
      return;
    }
    if (ruleInfoPOS.size() == 1) {
      // 如果只有一个规则，那么冲突率和重叠率都为0
      po.setId(ruleInfoPOS.get(0).getId());
      po.setConflict("0");
      po.setOverlap("0");
      po.setUpdateDatetime(LocalDateTime.now());
      po.setCompleted(1);
      ruleInfoService.updateByIdSelective(po);
      RuleOverviewPO infoPO = new RuleOverviewPO();
      infoPO.setTaskId(taskId);
      infoPO.setConflict("0");
      // 获取规则的打标的数量
      Long coverageCount = ruleUnlabeledResultService.countCoverageResult(taskId);
      // 获取总的未标注数据集的语料量
      Long totalCount = unlabelDataService.countUnlabelDataByTaskId(taskId);
      if (coverageCount == null || totalCount == null || totalCount == 0) {
        infoPO.setCoverage("0");
      } else {
        infoPO.setCoverage(CalcUtil.multiply(CalcUtil.divide(coverageCount, totalCount, 4), "100", 2));
      }
      infoPO.setAccuracy(ruleInfoPOS.get(0).getAccuracy());
      String testDataCoverage = calcGlobalTestDataCoverage(taskId);
      infoPO.setTestDataCoverage(testDataCoverage);
      infoPO.setUpdateDatetime(LocalDateTime.now());
      ruleOverviewService.insertRuleOverview(infoPO);
      return;
    }
    // 如果有多个规则，那么需要计算每个规则的冲突率和重叠率
    calcRuleGlobalParam(taskId, ruleInfoPOS);
    // 计算整个任务的规则概览
    calcGlobalTaskParam(taskId);
  }

  private void calcRuleGlobalParam(Long taskId, List<RuleInfoPO> ruleInfoPOS) {
    // 全部结果 ruleId -> {sentenceId -> labelId}
    Map<Long, Map<Long, Integer>> ruleTotalResult = new HashMap<>();
    for (RuleInfoPO ruleInfoPO : ruleInfoPOS) {
      // 每次取20000条数据，分页获取，防止内存溢出
      Long totalSize = ruleUnlabeledResultService.countByTaskIdAndRuleId(taskId, ruleInfoPO.getId());
      long totalPage = PageUtil.totalPage(totalSize, 20000);
      Map<Long, Integer> ruleResult = new HashMap<>((int) totalPage);
      for (int i = 0; i < totalPage; i++) {
        List<RuleUnlabeledResultPO> resultPOS = ruleUnlabeledResultService.simplePageByTaskIdAndRuleId(
            taskId, ruleInfoPO.getId(), i * 20000L, 20000);
        for (RuleUnlabeledResultPO unlabeledResultPO : resultPOS) {
          if (unlabeledResultPO.getLabelId() > -1) {
            ruleResult.put(unlabeledResultPO.getSentenceId(), unlabeledResultPO.getLabelId());
          }
        }
      }
      ruleTotalResult.put(ruleInfoPO.getId(), ruleResult);
    }
    // 计算冲突率和重叠率
    for (RuleInfoPO ruleInfoPO : ruleInfoPOS) {
      // 冲突 一条语料存在多个标签
      int conflict = 0;
      int overlap = 0;
      // 当前规则下每条语料对应的标签
      Map<Long, Integer> currentMap = ruleTotalResult.get(ruleInfoPO.getId());
      int total = currentMap.size();
      // 冲突率
      String conflictRate = "0";
      // 重叠率
      String overlapRate = "0";
      if (total > 0) {
        for (Map.Entry<Long, Integer> entry : currentMap.entrySet()) {
          long currentSentenceId = entry.getKey();
          Integer currentLabelId = entry.getValue();
          boolean isConflict = false;
          boolean isOverlap = false;
          for (Entry<Long, Map<Long, Integer>> totalEntry : ruleTotalResult.entrySet()) {
            if (totalEntry.getKey().equals(ruleInfoPO.getId())) {
              continue;
            }
            Map<Long, Integer> other = totalEntry.getValue();
            Integer otherLabel = other.get(currentSentenceId);
            if (otherLabel == null) {
              continue;
            }
            if (!currentLabelId.equals(otherLabel)) {
              isConflict = true;
            }
            if (currentLabelId.equals(otherLabel)) {
              isOverlap = true;
            }
            if (isConflict && isOverlap) {
              break;
            }
          }
          if (isConflict) {
            conflict++;
          }
          if (isOverlap) {
            overlap++;
          }
        }
        conflictRate = CalcUtil.divide(conflict, total, 4);
        overlapRate = CalcUtil.divide(overlap, total, 4);
      }
      // 更新规则信息
      RuleInfoPO newRuleInfo = new RuleInfoPO();
      newRuleInfo.setId(ruleInfoPO.getId());
      newRuleInfo.setConflict(CalcUtil.multiply(conflictRate, "100", 2));
      newRuleInfo.setOverlap(CalcUtil.multiply(overlapRate, "100", 2));
      newRuleInfo.setUpdateDatetime(LocalDateTime.now());
      ruleInfoService.updateByIdSelective(newRuleInfo);
    }
  }

  /**
   * 计算全局规则概览参数
   *
   * @param taskId 任务id
   */
  private void calcGlobalTaskParam(Long taskId) {
    // 获取测试集数据内容
    List<TestDataPO> testDataPOList = testDataService.getAllShowByTaskId(taskId);
    Map<Long, Integer> testData = testDataPOList.stream()
        .collect(Collectors.toMap(TestDataPO::getDataId, TestDataPO::getLabel));
    // 根据每个规则的标注结果，构建投票模型，获取最后的标签
    // 投票模型算法为：忽略-1（即未匹配到的记录），根据少数服从多数的原则，选取标签，如果票数一样，那么随机取
    List<RuleVotePO> voteResult = ruleResultService.getVoteByTaskId(taskId);
    // 最终投票结果
    Map<Long, Integer> voteResultMap = new HashMap<>(voteResult.size());
    // 语料冲突的数量，如果一条语料被多个规则标注，则记为冲突
    for (RuleVotePO ruleVotePO : voteResult) {
      String[] strings = ruleVotePO.getLabelVote().split(",");
      HashMap<String, Integer> map = new HashMap<>();
      int max = 0;
      int maxId = -1;
      for (String string : strings) {
        if ("-1".equals(string)) {
          continue;
        }
        if (map.containsKey(string)) {
          int count = map.get(string) + 1;
          map.put(string, count);
          if (count > max) {
            max = count;
            maxId = Integer.parseInt(string);
          }
        } else {
          map.put(string, 1);
          if (1 > max) {
            maxId = Integer.parseInt(string);
            max = 1;
          }
        }
      }
      voteResultMap.put(ruleVotePO.getSentenceId(), maxId);
    }
    int correct = 0;
    for (Entry<Long, Integer> entry : voteResultMap.entrySet()) {
      Integer exactLabel = testData.get(entry.getKey());
      if (exactLabel != null && exactLabel.equals(entry.getValue())) {
        correct++;
      }
    }
    // 正确率
    String correctRate = "0";
    if (!voteResultMap.isEmpty()) {
      correctRate = CalcUtil.divide(correct, voteResultMap.size(), 4);
    }
    String[] conflictAndCoverage = calcGlobalConflictAndCoverage(taskId);
    // 更新任务概览信息
    RuleOverviewPO infoPO = new RuleOverviewPO();
    infoPO.setTaskId(taskId);
    // 计算冲突率，冲突率=产生冲突语料量 / 总的已标注语料量
    infoPO.setConflict(CalcUtil.multiply(conflictAndCoverage[0], "100", 2));
    // 计算覆盖率，覆盖率=已标记语料量 / 总的未标注数据集语料量
    infoPO.setCoverage(CalcUtil.multiply(conflictAndCoverage[1], "100", 2));
    infoPO.setAccuracy(CalcUtil.multiply(correctRate, "100", 2));
    String testDataCoverage = calcGlobalTestDataCoverage(taskId);
    infoPO.setTestDataCoverage(testDataCoverage);
    infoPO.setUpdateDatetime(LocalDateTime.now());
    ruleOverviewService.insertRuleOverview(infoPO);
  }

  /**
   * 计算整体测试集的覆盖率
   *
   * @param taskId 任务id
   * @return 覆盖率
   */
  private String calcGlobalTestDataCoverage(Long taskId) {
    // 整体测试集的覆盖率计算方式：覆盖率= 已覆盖的展示的测试集数据量 / 总的展示测试集数据量
    Integer total = ruleResultService.countLabeledSentence(taskId);
    Long totalTestData = testDataService.countShowTestDataByTaskId(taskId);
    if (totalTestData != null && totalTestData > 0) {
      return CalcUtil.multiply(CalcUtil.divide(total, totalTestData, 4), "100", 2);
    } else {
      return "0";
    }
  }

  /**
   * 计算规则总览的冲突率和覆盖率，面向未标注数据集
   *
   * @param taskId 任务id
   * @return 冲突率和覆盖率
   */
  private String[] calcGlobalConflictAndCoverage(Long taskId) {
    // 覆盖数量,总的已标注语料量
    Long coverageCount = ruleUnlabeledResultService.countCoverageResult(taskId);
    // 计算冲突率
    Long conflictCount = ruleUnlabeledResultService.countConflictResult(taskId);
    // 获取总的未标注数据集的语料量
    Long totalCount = unlabelDataService.countUnlabelDataByTaskId(taskId);
    String conflictRate = "0";
    if (coverageCount > 0) {
      conflictRate = CalcUtil.divide(conflictCount, coverageCount, 4);
    }
    String coverageRate = "0";
    if (totalCount > 0) {
      coverageRate = CalcUtil.divide(coverageCount, totalCount, 4);
    }
    return new String[]{conflictRate, coverageRate};
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(
      RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }

  @Autowired
  public void setRuleOverviewService(RuleOverviewService ruleOverviewService) {
    this.ruleOverviewService = ruleOverviewService;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }
}
