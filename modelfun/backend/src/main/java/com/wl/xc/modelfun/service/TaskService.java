package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.req.TaskReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TaskInfoVO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/11 16:37
 */
public interface TaskService {

  /**
   * 创建任务
   *
   * @param taskReq 任务请求
   * @return 任务信息
   */
  TaskInfoVO createTask(TaskReq taskReq);

  /**
   * 根据任务信息查询任务
   *
   * @param taskReq 任务信息
   * @return 查询结果
   */
  List<TaskInfoVO> getTaskList(TaskReq taskReq);

  /**
   * 根据任务id删除任务
   *
   * @param taskReq 任务id
   * @return 删除结果
   */
  ResultVo<Boolean> deleteTask(TaskReq taskReq);

  boolean updateTask(TaskReq taskReq);
}
