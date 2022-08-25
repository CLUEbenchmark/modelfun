package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.commons.enums.AutoLabelType;
import com.wl.xc.modelfun.entities.po.AutoLabelResultPO;
import com.wl.xc.modelfun.entities.po.IntegrateLabelResultPO;
import com.wl.xc.modelfun.entities.po.SimpleAutoLabelResult;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.IntegrateLabelResultMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/29 23:13
 */
@Service
public class IntegrateLabelResultService
    extends ServiceImpl<IntegrateLabelResultMapper, IntegrateLabelResultPO> {

  public PageVO<IntegrateLabelResultPO> selectPageByTaskIdAndKeyword(IPage<?> page, IntegrateLabelResultPO po) {
    IPage<IntegrateLabelResultPO> result = baseMapper.selectPageByTaskIdAndKeyword(page, po);
    return PageUtil.convert(result);
  }

  public long countByTaskId(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<IntegrateLabelResultPO>query()
            .eq(IntegrateLabelResultPO.COL_TASK_ID, taskId));
  }

  public long countCorrectByTaskId(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<IntegrateLabelResultPO>query()
            .eq(IntegrateLabelResultPO.COL_TASK_ID, taskId)
            .eq(IntegrateLabelResultPO.COL_DATA_TYPE, AutoLabelType.CORRECT.getType()));
  }

  public int countLabelByTaskId(Long taskId) {
    return baseMapper.countLabelByTaskId(taskId);
  }

  public void saveBatchNoLog(List<IntegrateLabelResultPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.IntegrateLabelResultForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public void deleteByTaskId(Long taskId) {
    baseMapper.delete(
        Wrappers.<IntegrateLabelResultPO>query()
            .eq(IntegrateLabelResultPO.COL_TASK_ID, taskId));
  }

  public List<AutoLabelResultPO> pageLabelCorrectByTaskId(Long taskId, long offset, int size) {
    return baseMapper.pageLabelCorrectByTaskId(taskId, offset, size);
  }

  public List<SimpleAutoLabelResult> pageCorrectByTaskId(Long taskId, long offset, int size) {
    return baseMapper.pageCorrectByTaskId(taskId, offset, size);
  }

  public int copyAutoLabelResultFromTemplate(Long srcTask, Long destTask, Integer datasetId) {
    return baseMapper.copyAutoLabelResultFromTemplate(srcTask, destTask, datasetId);
  }
}
