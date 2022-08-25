package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.IntegrationResultPO;
import com.wl.xc.modelfun.entities.po.IntegrationWithRule;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.IntegrationResultMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class IntegrationResultService extends ServiceImpl<IntegrationResultMapper, IntegrationResultPO> {


  public PageVO<IntegrationWithRule> getIntegrationPage(IPage<IntegrationWithRule> page, Long taskId) {
    List<IntegrationWithRule> result = baseMapper.pageResultByTaskId(page, taskId);
    page.setRecords(result);
    return PageUtil.convert(page);
  }

  public int deleteByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<IntegrationResultPO>query()
        .eq(IntegrationResultPO.COL_TASK_ID, taskId));
  }

}



