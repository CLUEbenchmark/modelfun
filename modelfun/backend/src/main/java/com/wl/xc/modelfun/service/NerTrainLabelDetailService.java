package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.NerTrainLabelDetailPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.NerTrainLabelDetailMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/6/9 18:08
 */
@Service
public class NerTrainLabelDetailService extends ServiceImpl<NerTrainLabelDetailMapper, NerTrainLabelDetailPO> {

  public PageVO<NerTrainLabelDetailPO> pageByTrainLabelId(Page<NerTrainLabelDetailPO> page, Long trainLabelId) {
    Page<NerTrainLabelDetailPO> poPage = baseMapper.selectPage(page,
        Wrappers.<NerTrainLabelDetailPO>query().eq(NerTrainLabelDetailPO.COL_TRAIN_LABEL_ID, trainLabelId));
    return PageUtil.convert(poPage);
  }

  public void saveBatchNoLog(List<NerTrainLabelDetailPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.NerTrainLabelDetailForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public int copyDataFromTemplate(Long srcTrainId, Long destTrainId) {
    return baseMapper.copyDataFromTemplate(srcTrainId, destTrainId);
  }
}
