package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.NerAutoLabelMapPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelWithDesPO;
import com.wl.xc.modelfun.mapper.NerAutoLabelMapMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/5/25 18:37
 */
@Service
public class NerAutoLabelMapService extends ServiceImpl<NerAutoLabelMapMapper, NerAutoLabelMapPO> {

  public List<NerDataLabelWithDesPO> getLabelResultByType(Long taskId, List<Long> sentenceIds, Integer dataType) {
    return baseMapper.getLabelResult(taskId, sentenceIds, dataType);
  }

  public void saveForBatchNoLog(List<NerAutoLabelMapPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.NerAutoLabelMapForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public int deleteByTaskId(Long taskId) {
    return baseMapper.delete(
        Wrappers.<NerAutoLabelMapPO>query().eq(NerAutoLabelMapPO.COL_TASK_ID, taskId));
  }

  public int deleteBySentenceId(Long taskId, Long dataId, Integer dataType) {
    return baseMapper.delete(
        Wrappers.<NerAutoLabelMapPO>query()
            .eq(NerAutoLabelMapPO.COL_TASK_ID, taskId)
            .eq(NerAutoLabelMapPO.COL_DATA_TYPE, dataType)
            .eq(NerAutoLabelMapPO.COL_SENTENCE_ID, dataId));
  }

  public void deleteByTaskIdAndType(Long taskId, int dataType) {
    baseMapper.delete(
        Wrappers.<NerAutoLabelMapPO>query()
            .eq(NerAutoLabelMapPO.COL_TASK_ID, taskId)
            .eq(NerAutoLabelMapPO.COL_DATA_TYPE, dataType));
  }

  public int copyDataFromTemplate(Long srcTask, Long destTask) {
    return baseMapper.copyDataFromTemplate(srcTask, destTask);
  }
}
