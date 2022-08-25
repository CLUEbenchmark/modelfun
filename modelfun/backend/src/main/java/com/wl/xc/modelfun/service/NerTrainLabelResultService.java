package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.NerTrainLabelResultPO;
import com.wl.xc.modelfun.mapper.NerTrainLabelResultMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/6/9 18:08
 */
@Service
public class NerTrainLabelResultService extends ServiceImpl<NerTrainLabelResultMapper, NerTrainLabelResultPO> {

  public List<NerTrainLabelResultPO> selectByTrainRecordId(Long trainRecordId) {
    return baseMapper.selectList(
        Wrappers.<NerTrainLabelResultPO>query().eq(NerTrainLabelResultPO.COL_TRAIN_RECORD_ID, trainRecordId));
  }

  public void saveBatchNoLog(List<NerTrainLabelResultPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.NerTrainLabelResultForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }


  public List<NerTrainLabelResultPO> getTemplateByTrainRecordId(Long trainRecordId) {
    return baseMapper.getTemplateByTrainRecordId(trainRecordId);
  }
}
