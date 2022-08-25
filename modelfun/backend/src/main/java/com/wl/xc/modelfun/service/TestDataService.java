package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.TestDataMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 18:56
 */
@Service
public class TestDataService extends ServiceImpl<TestDataMapper, TestDataPO> {

  public PageVO<TestDataPO> pageDatasetDetail(Page<TestDataPO> page, TestDataPO po) {
    page.orders().add(OrderItem.asc(TestDataPO.COL_DATA_ID));
    Page<TestDataPO> poPage =
        baseMapper.selectPage(
            page, Wrappers.<TestDataPO>query()
                .eq(TestDataPO.COL_TASK_ID, po.getTaskId())
                .eq(TestDataPO.COL_DATA_TYPE, po.getDataType())
                .eq(po.getLabel() != null, TestDataPO.COL_LABEL, po.getLabel())
                .like(StringUtils.isNotBlank(po.getSentence()), TestDataPO.COL_SENTENCE, po.getSentence()));
    return PageUtil.convert(poPage);
  }

  public long countByTaskIdAndType(Long taskId, Integer dataType) {
    return baseMapper.selectCount(
        Wrappers.<TestDataPO>query()
            .eq(TestDataPO.COL_TASK_ID, taskId)
            .eq(TestDataPO.COL_DATA_TYPE, dataType));
  }

  /**
   * 获取验证集数据，即可见部分，原先只需要拆成两份，所以用show_data字段来标记，现在需要拆成3分，增加了dataType字段，show_data废弃
   *
   * @param taskId 任务id
   * @return 验证集数据数量
   */
  public Long countShowTestDataByTaskId(Long taskId) {
    return countByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
  }

  /**
   * 获取测试集数据，即不可见部分
   *
   * @param taskId 任务id
   * @return 测试集数据数量
   */
  public Long countUnShowTestDataByTaskId(Long taskId) {
    return countByTaskIdAndType(taskId, DatasetType.TEST_UN_SHOW.getType());
  }

  public Long groupCountLabelByTaskId(Long taskId) {
    return baseMapper.groupCountLabelByTaskId(taskId);
  }

  public int deleteTestDataByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<TestDataPO>query().eq(TestDataPO.COL_TASK_ID, taskId));
  }

  /**
   * 获取验证集和测试集数据
   *
   * @param taskId 任务id
   * @return 验证集和测试集数据
   */
  public List<TestDataPO> getAllByTaskId(Long taskId) {
    return baseMapper.selectList(Wrappers.<TestDataPO>query()
        .eq(TestDataPO.COL_TASK_ID, taskId)
        .in(TestDataPO.COL_DATA_TYPE, DatasetType.TEST_SHOW.getType(), DatasetType.TEST_UN_SHOW.getType())
        .orderByAsc(TestDataPO.COL_ID));
  }

  public List<TestDataPO> getAllUnShowByTaskId(Long taskId) {
    return baseMapper.selectList(
        Wrappers.<TestDataPO>query().eq(TestDataPO.COL_TASK_ID, taskId)
            .eq(TestDataPO.COL_DATA_TYPE, DatasetType.TEST_UN_SHOW.getType())
            .orderByAsc(TestDataPO.COL_ID));
  }

  public List<TestDataPO> getAllShowByTaskId(Long taskId) {
    return baseMapper.selectList(
        Wrappers.<TestDataPO>query().eq(TestDataPO.COL_TASK_ID, taskId)
            .eq(TestDataPO.COL_DATA_TYPE, DatasetType.TEST_SHOW.getType())
            .orderByAsc(TestDataPO.COL_ID));
  }

  public void saveForBatchNoLog(List<TestDataPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.TestDataForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public List<TestDataPO> selectNoGptCache(Long taskId, boolean show) {
    return baseMapper.selectNoGptCache(taskId, show ? 1 : 0);
  }

  public TestDataPO getByTaskIdAndDataIdAndType(Long taskId, Long dataId, Integer dataType) {
    return baseMapper.selectOne(Wrappers.<TestDataPO>query()
        .eq(TestDataPO.COL_TASK_ID, taskId)
        .eq(TestDataPO.COL_DATA_TYPE, dataType)
        .eq(TestDataPO.COL_DATA_ID, dataId));
  }

  public List<TestDataPO> pageTestData(Long taskId, int dataType, long offset, int size) {
    return baseMapper.pageTestData(taskId, dataType, offset, size);
  }

  public void insertAndAutoIncrement(TestDataPO po) {
    baseMapper.insertAndAutoIncrement(po);
  }

  public long countUnLabelTrainData(Long taskId) {
    return baseMapper.selectCount(Wrappers.<TestDataPO>query()
        .eq(TestDataPO.COL_TASK_ID, taskId)
        .eq(TestDataPO.COL_DATA_TYPE, DatasetType.TRAIN.getType())
        .isNull(TestDataPO.COL_LABEL));
  }

  public List<TestDataPO> getTrainCorrectData(Long taskId, Long trainRecordId) {
    return baseMapper.getTrainCorrectData(taskId, trainRecordId);
  }

  public int copyTestDataFromTemplate(Long srcTaskId, Long destTaskId, Integer datasetId) {
    return baseMapper.copyTestDataFromTemplate(srcTaskId, destTaskId, datasetId);
  }
}
