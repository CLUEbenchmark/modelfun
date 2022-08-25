package com.wl.xc.modelfun.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.UnlabelDataForBatchMapper;
import com.wl.xc.modelfun.mapper.UnlabelDataMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/11 18:56
 */
@Service
public class UnlabelDataService extends ServiceImpl<UnlabelDataMapper, UnlabelDataPO> {

  private UnlabelDataForBatchMapper unlabelDataForBatchMapper;

  public PageVO<UnlabelDataPO> pageDatasetDetail(Page<UnlabelDataPO> page, UnlabelDataPO po) {
    page.orders().add(OrderItem.asc(TestDataPO.COL_DATA_ID));
    QueryWrapper<UnlabelDataPO> wrapper =
        Wrappers.<UnlabelDataPO>query().eq(DatasetDetailPO.COL_TASK_ID, po.getTaskId());
    wrapper.like(
        StrUtil.isNotBlank(po.getSentence()), UnlabelDataPO.COL_SENTENCE, po.getSentence());
    Page<UnlabelDataPO> poPage = baseMapper.selectPage(page, wrapper);
    return PageUtil.convert(poPage);
  }

  public Long countUnlabelDataByTaskId(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<UnlabelDataPO>query().eq(UnlabelDataPO.COL_TASK_ID, taskId));
  }

  public Long countLabeledDataByTaskId(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<UnlabelDataPO>query()
            .eq(UnlabelDataPO.COL_TASK_ID, taskId)
            .gt(UnlabelDataPO.COL_LABEL, -1));
  }

  public int deleteUnlabelDataByTaskId(Long taskId) {
    return baseMapper.delete(
        Wrappers.<UnlabelDataPO>query().eq(UnlabelDataPO.COL_TASK_ID, taskId));
  }

  public List<UnlabelDataPO> getAllByTaskId(Long taskId) {
    return baseMapper.selectList(Wrappers.<UnlabelDataPO>query().eq(UnlabelDataPO.COL_TASK_ID, taskId));
  }

  @Transactional(rollbackFor = Exception.class)
  public void saveBatchNoLog(List<UnlabelDataPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.UnlabelDataForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateBatchNoLog(List<UnlabelDataPO> list) {
    String statement = "com.wl.xc.modelfun.mapper.UnlabelDataForBatchMapper.updateByPrimaryKeySelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public List<UnlabelDataPO> pageByTaskId(long taskId, long offset, int pageSize) {
    return baseMapper.pageByTaskId(taskId, offset, pageSize);
  }

  public void clearOldLabel(long taskId) {
    baseMapper.clearOldLabel(taskId);
  }

  /**
   * 根据数据语料ID批量获取数据，SQL语法为：select * from table where data_id in (1,2,3,4,5)
   *
   * @param taskId  任务ID
   * @param dataIds 语料ID集合
   * @return 数据集合
   */
  public List<UnlabelDataPO> listByDataId(Long taskId, Collection<Long> dataIds) {
    return unlabelDataForBatchMapper.listByDataId(taskId, dataIds);
  }

  public List<UnlabelDataPO> selectNoGptCache(Long taskId) {
    return baseMapper.selectNoGptCache(taskId);
  }

  public int deleteByTaskAndDataId(Long taskId, Long dataId) {
    return baseMapper.delete(
        Wrappers.<UnlabelDataPO>query()
            .eq(UnlabelDataPO.COL_TASK_ID, taskId)
            .eq(UnlabelDataPO.COL_DATA_ID, dataId));
  }

  public Long getMaxDataIdByTaskId(Long taskId) {
    return baseMapper.getMaxIdByTask(taskId);
  }

  @Autowired
  public void setUnlabelDataForBatchMapper(UnlabelDataForBatchMapper unlabelDataForBatchMapper) {
    this.unlabelDataForBatchMapper = unlabelDataForBatchMapper;
  }

  public int copyUnlabelDataFromTemplate(Long srcTask, Long destTask, Integer datasetId) {
    return baseMapper.copyUnlabelDataFromTemplate(srcTask, destTask, datasetId);
  }
}
