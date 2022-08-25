package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.TaskExpertPO;
import com.wl.xc.modelfun.mapper.TaskExpertMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/14 15:56
 */
@Service
public class TaskExpertService extends ServiceImpl<TaskExpertMapper, TaskExpertPO> {


  /**
   * 根据任务ID删除所有专家知识
   *
   * @param taskId 任务ID
   */
  public void deleteAllByTaskId(Long taskId) {
    baseMapper.delete(Wrappers.<TaskExpertPO>query().eq(TaskExpertPO.COL_TASK_ID, taskId));
  }

  /**
   * 根据任务ID查询所有专家知识
   *
   * @param taskId 任务ID
   * @return 专家知识列表
   */
  public List<TaskExpertPO> getAllByTaskId(Long taskId) {
    return baseMapper.selectList(Wrappers.<TaskExpertPO>query().eq(TaskExpertPO.COL_TASK_ID, taskId));
  }


}
