package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.LabelInfoMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class LabelInfoService extends ServiceImpl<LabelInfoMapper, LabelInfoPO> {

  public PageVO<LabelInfoPO> pageLabelInfo(IPage<LabelInfoPO> page, LabelInfoPO labelInfoPO) {
    page.orders().add(OrderItem.asc(LabelInfoPO.COL_LABEL_ID));
    IPage<LabelInfoPO> iPage = baseMapper.pageLabelInfo(page, labelInfoPO);
    return PageUtil.convert(iPage);
  }

  public long countLabelInfoByTaskId(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<LabelInfoPO>query().eq(LabelInfoPO.COL_TASK_ID, taskId));
  }

  public int deleteLabelInfoByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<LabelInfoPO>query().eq(LabelInfoPO.COL_TASK_ID, taskId));
  }

  public void saveBatchNoLog(List<LabelInfoPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.LabelInfoForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public List<LabelInfoPO> selectListByTaskId(Long taskId) {
    return baseMapper.selectList(Wrappers.<LabelInfoPO>query().eq(LabelInfoPO.COL_TASK_ID, taskId));
  }

  public LabelInfoPO selectOneByTaskAndLabel(Long taskId, Integer labelId) {
    return baseMapper.selectOne(Wrappers.<LabelInfoPO>query().eq(LabelInfoPO.COL_TASK_ID, taskId)
        .eq(LabelInfoPO.COL_LABEL_ID, labelId));
  }

  public int copyLabelInfoFromTemplate(Long srcTask, Long destTask, Integer datasetId) {
    return baseMapper.copyLabelInfoFromTemplate(srcTask, destTask, datasetId);
  }
}
