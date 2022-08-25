package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.TrainLabelSentenceInfoPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.TrainLabelSentenceInfoMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/6/9 19:20
 */
@Service
public class TrainLabelSentenceInfoService extends ServiceImpl<TrainLabelSentenceInfoMapper, TrainLabelSentenceInfoPO> {

  public List<TrainLabelSentenceInfoPO> selectByTrainRecordId(Long trainRecordId, Collection<Long> dataIds) {
    return baseMapper.selectList(
        Wrappers.<TrainLabelSentenceInfoPO>query()
            .eq(TrainLabelSentenceInfoPO.COL_TRAIN_RECORD_ID, trainRecordId)
            .in(TrainLabelSentenceInfoPO.COL_DATA_ID, dataIds));
  }

  public List<TrainLabelSentenceInfoPO> selectAllByTrainRecordId(Long trainRecordId) {
    return baseMapper.selectList(
        Wrappers.<TrainLabelSentenceInfoPO>query()
            .eq(TrainLabelSentenceInfoPO.COL_TRAIN_RECORD_ID, trainRecordId));
  }

  public void saveBatchNoLog(List<TrainLabelSentenceInfoPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.TrainLabelSentenceInfoForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public PageVO<TrainLabelSentenceInfoPO> pageByLabel(IPage<TrainLabelSentenceInfoPO> page,
      TrainLabelSentenceInfoPO po) {
    IPage<TrainLabelSentenceInfoPO> result = baseMapper.selectPage(
        page,
        Wrappers.<TrainLabelSentenceInfoPO>query()
            .eq(TrainLabelSentenceInfoPO.COL_TRAIN_RECORD_ID, po.getTrainRecordId())
            .eq(TrainLabelSentenceInfoPO.COL_LABEL_ACTUAL, po.getLabelActual())
            .eq(TrainLabelSentenceInfoPO.COL_LABEL_PREDICT, po.getLabelPredict()));
    return PageUtil.convert(result);
  }

  public int copyDataFromTemplate(Long srcTrainId, Long destTrainId) {
    return baseMapper.copyDataFromTemplate(srcTrainId, destTrainId);
  }
}
