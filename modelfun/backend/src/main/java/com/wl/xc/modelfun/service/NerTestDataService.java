package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.NerTestDataPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.NerTestDataMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/5/24 14:48
 */
@Service
public class NerTestDataService extends ServiceImpl<NerTestDataMapper, NerTestDataPO> {

  public PageVO<NerTestDataPO> pageNerTestData(IPage<NerTestDataPO> page, NerTestDataPO nerTestDataPO, Integer labelId,
      boolean isDesc) {
    IPage<NerTestDataPO> result = baseMapper.pageNerTestData(page, nerTestDataPO, labelId, isDesc);
    return PageUtil.convert(result);
  }


  public void saveForBatchNoLog(List<NerTestDataPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.NerTestDataForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public int deleteByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<NerTestDataPO>query().eq(NerTestDataPO.COL_TASK_ID, taskId));
  }

  public Long getMaxSentenceId(Long taskId, Integer dataType) {
    return baseMapper.selectMaxSentenceId(taskId, dataType);
  }

  public Long countByTaskIdAndType(Long taskId, Integer dataType) {
    return baseMapper.selectCount(Wrappers.<NerTestDataPO>query().eq(NerTestDataPO.COL_TASK_ID, taskId)
        .eq(NerTestDataPO.COL_DATA_TYPE, dataType));
  }

  public int countUnLabeledData(Long taskId, int dataType) {
    return baseMapper.countUnLabeledData(taskId, dataType);
  }

  public int copyDataFromTemplate(Long srcTask, Long destTask) {
    return baseMapper.copyDataFromTemplate(srcTask, destTask);
  }
}
