package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.RuleResultPO;
import com.wl.xc.modelfun.entities.po.RuleUnlabeledResultPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.PageUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用于单条规则打标签并且计算规则参数的类。
 *
 * <p>单独写一个类主要是为了把规则处理器的耗时动作排除在事务之外。
 *
 * @version 1.0
 * @date 2022/4/27 22:42
 */
@Slf4j
@Component
public class SingleRuleLabelService {

  private TestDataService testDataService;

  private RuleResultService ruleResultService;

  private RuleInfoService ruleInfoService;

  private RuleUnlabeledResultService ruleUnlabeledResultService;

  private UnlabelDataService unlabelDataService;

  @Transactional(rollbackFor = Exception.class)
  public void label(RuleTask ruleTask, RuleTaskHandler handler) {
    RuleInfoPO ruleInfo = ruleTask.getRuleInfo();
    // 打标签之前先把原来的结果删除
    ruleResultService.deleteByTaskIdAndRuleId(ruleTask.getTaskId(), ruleInfo.getId());
    ruleUnlabeledResultService.deleteByTaskIdAndRuleId(ruleTask.getTaskId(), ruleInfo.getId());
    RuleInfoPO ruleInfoPO = new RuleInfoPO();
    // 给测试集数据打标签并且计算规则参数
    calcTestData(ruleTask, handler, ruleInfoPO);
    // 计算未标注数据结果
    calcUnlabeledData(ruleTask, handler, ruleInfoPO);
    // 设置规则为已完成，单条规则计算完成
    ruleInfoPO.setCompleted(1);
    ruleInfoPO.setUpdateDatetime(LocalDateTime.now());
    // 更新规则的信息，包括准确率、覆盖率、未标注数据覆盖率。冲突率和重叠率得用全局任务计算，需要统计其他规则的结果
    ruleInfoService.updateByIdSelective(ruleInfoPO);
    handler.afterLabel();
  }

  private void calcTestData(RuleTask ruleTask, RuleTaskHandler handler, RuleInfoPO ruleInfoPO) {
    RuleInfoPO ruleInfo = ruleTask.getRuleInfo();
    // 获取测试集数据
    List<TestDataPO> testDataPOList = testDataService.getAllByTaskId(ruleInfo.getTaskId());
    // 计算测试集标注结果，内容为 语料ID -》 标签ID
    // 规则打的标签为-1的数量
    long unlabeledCount = 0;
    // 规则打的标签和测试集一样的数量
    long correctCount = 0;
    // 规则上标签的数量
    long labelCount = 0;
    List<RuleResultPO> ruleResultPOList = new ArrayList<>();
    Set<Integer> labelSet = new HashSet<>();
    List<TestDataPO> labelResult = new ArrayList<>(testDataPOList.size());
    for (TestDataPO testDataPO : testDataPOList) {
      int label = handler.label(testDataPO.getSentence(), DatasetType.TEST);
      if (DatasetType.TEST_SHOW.getType().equals(testDataPO.getDataType())) {
        if (label == -1) {
          unlabeledCount++;
        } else {
          labelCount++;
          labelSet.add(label);
          labelResult.add(testDataPO);
        }
        int testLabel = testDataPO.getLabel();
        if (label == testLabel) {
          correctCount++;
        }
      }
      RuleResultPO resultPO = new RuleResultPO();
      resultPO.setTaskId(ruleInfo.getTaskId());
      resultPO.setRuleId(ruleInfo.getId());
      resultPO.setSentenceId(testDataPO.getDataId());
      resultPO.setLabelId(label);
      resultPO.setShowData(testDataPO.getShowData());
      resultPO.setDataType(testDataPO.getDataType());
      ruleResultPOList.add(resultPO);
    }
    // 测试集中，打上的标签对应的测试集语料量
    int totalCountOfLabel = 0;
    for (TestDataPO po : testDataPOList) {
      if (1 == po.getShowData()) {
        if (labelSet.contains(po.getLabel())) {
          totalCountOfLabel++;
        }
      }
    }
    long groupLabelCount = 0;
    for (TestDataPO testDataPO : labelResult) {
      if (labelSet.contains(testDataPO.getLabel())) {
        groupLabelCount++;
      }
    }
    // 测试集打标签的结果批量插入数据库
    ruleResultService.saveForBatchNoLog(ruleResultPOList);
    log.info(
        "[SingleRuleLabelService.calcTestData] labelCount:{}, correctCount:{}, unlabeledCount:{}, totalCountOfLabel:{}, groupLabelCount:{}",
        labelCount, correctCount, unlabeledCount, totalCountOfLabel, groupLabelCount);
    // 计算单条规则的准确率，计算方式为：标注正确的语料数量 / 打上标签的语料数量（即标签不为-1的语料数量）
    String accuracy = "0";
    if (labelCount > 0) {
      accuracy = CalcUtil.divide(correctCount, labelCount, 4);
    }
    // 计算单条规则的覆盖率，计算方式为：属于该测试集的标签的打上标签的数量 / 标签对应的测试集语料量
    String coverage = "0";
    if (totalCountOfLabel > 0) {
      coverage = CalcUtil.divide(groupLabelCount, totalCountOfLabel, 4);
    }
    ruleInfoPO.setId(ruleInfo.getId());
    ruleInfoPO.setAccuracy(CalcUtil.multiply(accuracy, "100", 2));
    ruleInfoPO.setCoverage(CalcUtil.multiply(coverage, "100", 2));
  }

  private void calcUnlabeledData(
      RuleTask ruleTask, RuleTaskHandler handler, RuleInfoPO ruleInfoPO) {
    RuleInfoPO ruleInfo = ruleTask.getRuleInfo();
    // 未标注数据的数量
    Long unlabeledDataCount = unlabelDataService.countUnlabelDataByTaskId(ruleInfo.getTaskId());
    long totalPage = PageUtil.totalPage(unlabeledDataCount, 10000L);
    int labelCount = 0;
    // 每次从数据库中取10000条数据进行计算，防止内存溢出
    for (int i = 0; i < totalPage; i++) {
      long offset = i * 10000L;
      List<UnlabelDataPO> unlabelDataPOList =
          unlabelDataService.pageByTaskId(ruleInfo.getTaskId(), offset, 10000);
      int size = unlabelDataPOList.size();
      List<RuleUnlabeledResultPO> ruleUnlabeledResultPOList = new ArrayList<>(size);
      for (UnlabelDataPO po : unlabelDataPOList) {
        int label = handler.label(po.getSentence(), DatasetType.UNLABELED);
        if (label > -1) {
          labelCount++;
        }
        RuleUnlabeledResultPO resultPO = new RuleUnlabeledResultPO();
        resultPO.setTaskId(ruleInfo.getTaskId());
        resultPO.setRuleId(ruleInfo.getId());
        resultPO.setSentenceId(po.getDataId());
        resultPO.setLabelId(label);
        ruleUnlabeledResultPOList.add(resultPO);
      }
      if (!ruleUnlabeledResultPOList.isEmpty()) {
        ruleUnlabeledResultService.saveForBatchNoLog(ruleUnlabeledResultPOList);
      }
    }
    // 计算未标注数据的覆盖率，计算方式为：已标注的数量 / 未标注数总条数
    String unlabeledCoverage = "0";
    if (unlabeledDataCount > 0) {
      unlabeledCoverage = CalcUtil.divide(labelCount, unlabeledDataCount, 4);
    }
    ruleInfoPO.setUnlabeledCoverage(CalcUtil.multiply(unlabeledCoverage, "100", 2));
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }
}
