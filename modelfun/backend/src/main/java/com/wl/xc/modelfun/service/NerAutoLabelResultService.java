package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.NerAutoLabelResultPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.NerAutoLabelResultMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/5/25 18:37
 */
@Service
public class NerAutoLabelResultService extends ServiceImpl<NerAutoLabelResultMapper, NerAutoLabelResultPO> {

  public PageVO<NerAutoLabelResultPO> pageNerAutoLabelResult(Page<NerAutoLabelResultPO> page, NerAutoLabelResultPO po,
      Integer labelId) {
    IPage<NerAutoLabelResultPO> result = baseMapper.pageAutoLabelResult(page, po, labelId);
    return PageUtil.convert(result);
  }

  public void saveForBatchNoLog(List<NerAutoLabelResultPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.NerAutoLabelResultForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public int deleteByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<NerAutoLabelResultPO>query().eq(NerAutoLabelResultPO.COL_TASK_ID, taskId));
  }

  public long countByTaskIdAndType(Long taskId, Integer dataType) {
    return baseMapper.selectCount(Wrappers.<NerAutoLabelResultPO>query().eq(NerAutoLabelResultPO.COL_TASK_ID, taskId)
        .eq(NerAutoLabelResultPO.COL_DATA_TYPE, dataType));
  }

  public void deleteByTaskIdAndType(Long taskId, int dataType) {
    baseMapper.delete(Wrappers.<NerAutoLabelResultPO>query().eq(NerAutoLabelResultPO.COL_TASK_ID, taskId)
        .eq(NerAutoLabelResultPO.COL_DATA_TYPE, dataType));
  }

  public int copyAutoLabelResultFromTemplate(Long srcTask, Long destTask) {
    return baseMapper.copyAutoLabelResultFromTemplate(srcTask, destTask);
  }
}
