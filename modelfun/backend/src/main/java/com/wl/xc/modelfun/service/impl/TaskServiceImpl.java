package com.wl.xc.modelfun.service.impl;

import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.req.TaskReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TaskInfoVO;
import com.wl.xc.modelfun.service.IntegrateLabelResultService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TaskService;
import com.wl.xc.modelfun.service.TrainResultService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/11 16:37
 */
@Service
public class TaskServiceImpl implements TaskService {

  private TaskInfoService taskInfoService;

  private TrainResultService trainResultService;

  private UnlabelDataService unlabelDataService;

  private IntegrateLabelResultService integrateLabelResultService;

  @Override
  public TaskInfoVO createTask(TaskReq taskReq) {
    TaskInfoPO infoPO = new TaskInfoPO();
    taskReq.setId(null);
    BeanCopyUtil.copy(taskReq, infoPO);
    infoPO.setUpdateDatetime(LocalDateTime.now());
    infoPO.setCreateDatetime(LocalDateTime.now());
    boolean result = taskInfoService.save(infoPO);
    if (result) {
      return getTaskInfoVOByTask(infoPO);
    } else {
      return null;
    }
  }

  @Override
  public List<TaskInfoVO> getTaskList(TaskReq taskReq) {
    TaskInfoPO infoPO = new TaskInfoPO();
    BeanCopyUtil.copy(taskReq, infoPO);
    List<TaskInfoVO> list = taskInfoService.getTaskInfoListByNameAndDes(infoPO);
    list.forEach(v -> v.setUnlabeledCount(Math.max(0, v.getUnlabeledCount() - v.getLabeledCount())));
    return list;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> deleteTask(TaskReq taskReq) {
    boolean delete = taskInfoService.logicDelete(taskReq.getId(), "");
    return delete ? ResultVo.createSuccess(true) : ResultVo.create("删除失败", -1, false, false);
  }

  @Override
  public boolean updateTask(TaskReq taskReq) {
    TaskInfoPO infoPO = new TaskInfoPO();
    infoPO.setTaskType(null);
    BeanCopyUtil.copy(taskReq, infoPO);
    infoPO.setUpdateDatetime(LocalDateTime.now());
    return taskInfoService.updateById(infoPO);
  }

  private TaskInfoVO getTaskInfoVOByTask(TaskInfoPO taskInfoPO) {
    TaskInfoVO taskInfoVO = new TaskInfoVO();
    BeanCopyUtil.copy(taskInfoPO, taskInfoVO);
    return taskInfoVO;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setTrainResultService(TrainResultService trainResultService) {
    this.trainResultService = trainResultService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setIntegrateLabelResultService(
      IntegrateLabelResultService integrateLabelResultService) {
    this.integrateLabelResultService = integrateLabelResultService;
  }
}
