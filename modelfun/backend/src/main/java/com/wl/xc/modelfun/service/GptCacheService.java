package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.GptCachePO;
import com.wl.xc.modelfun.mapper.GptCacheMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/5/16 11:33
 */
@Service
public class GptCacheService extends ServiceImpl<GptCacheMapper, GptCachePO> {

  public List<Long> getCacheIds(Long taskId, int type) {
    return baseMapper.selectCachedId(taskId, type);
  }

  public void deleteCache(Long taskId) {
    baseMapper.delete(Wrappers.<GptCachePO>query().eq(GptCachePO.COL_TASK_ID, taskId));
  }

  public void deleteCacheByRule(Long taskId, Long ruleId) {
    baseMapper.delete(Wrappers.<GptCachePO>query().eq(GptCachePO.COL_TASK_ID, taskId)
        .eq(GptCachePO.COL_RULE_ID, ruleId));
  }


}
