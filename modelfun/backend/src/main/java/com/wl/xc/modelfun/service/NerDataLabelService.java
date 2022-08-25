package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.NerDataLabelPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelWithDesPO;
import com.wl.xc.modelfun.mapper.NerDataLabelMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/5/24 14:57
 */
@Service
public class NerDataLabelService extends ServiceImpl<NerDataLabelMapper, NerDataLabelPO> {

  public List<NerDataLabelWithDesPO> getBySentenceIdAndType(Long taskId, List<Long> sentenceIds, Integer dataType) {
    return baseMapper.getBySentenceId(taskId, sentenceIds, dataType);
  }

  public void saveForBatchNoLog(List<NerDataLabelPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.NerDataLabelForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public int deleteAllByTaskId(Long taskId) {
    return this.baseMapper.delete(
        Wrappers.<NerDataLabelPO>query().eq(NerDataLabelPO.COL_TASK_ID, taskId));
  }

  public int deleteBySentenceId(Long taskId, Long sentenceId, Integer dataType) {
    return this.baseMapper.delete(
        Wrappers.<NerDataLabelPO>query()
            .eq(NerDataLabelPO.COL_TASK_ID, taskId)
            .eq(NerDataLabelPO.COL_DATA_TYPE, dataType)
            .eq(NerDataLabelPO.COL_SENTENCE_ID, sentenceId));
  }

  public int copyDataFromTemplate(Long srcTask, Long destTask) {
    return baseMapper.copyDataFromTemplate(srcTask, destTask);
  }
}
