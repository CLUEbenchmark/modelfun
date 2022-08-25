package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.commons.validation.group.Create;
import com.wl.xc.modelfun.commons.validation.group.Delete;
import com.wl.xc.modelfun.commons.validation.group.Retrieve;
import com.wl.xc.modelfun.commons.validation.group.Update;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.req.TaskReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TaskInfoVO;
import com.wl.xc.modelfun.service.TaskService;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/11 13:12
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
public class TaskController {

  private TaskService taskService;

  @Operation(method = "POST", summary = "创建任务")
  @PostMapping("/create")
  public ResultVo<TaskInfoVO> createTask(@Validated({Create.class}) @RequestBody TaskReq taskReq) {
    LoginUserInfo user = ServletUserHolder.getUserByContext();
    taskReq.setUserId(user.getUserId());
    TaskInfoVO result = taskService.createTask(taskReq);
    return ResultVo.createSuccess(result);
  }

  @Operation(method = "POST", summary = "更新任务信息")
  @PostMapping("/update")
  public ResultVo<Boolean> updateTask(@Validated({Update.class}) @RequestBody TaskReq taskReq) {
    LoginUserInfo user = ServletUserHolder.getUserByContext();
    taskReq.setUserId(user.getUserId());
    return ResultVo.createSuccess(taskService.updateTask(taskReq));
  }

  @Operation(method = "POST", summary = "查询任务列表")
  @PostMapping("/list")
  public ResultVo<List<TaskInfoVO>> getTaskList(@Validated({Retrieve.class}) @RequestBody TaskReq taskReq) {
    return ResultVo.createSuccess(taskService.getTaskList(taskReq));
  }

  @Operation(method = "POST", summary = "根据任务ID删除任务")
  @PostMapping("/delete")
  public ResultVo<Boolean> deleteTask(@Validated({Delete.class}) @RequestBody TaskReq taskReq) {
    return taskService.deleteTask(taskReq);
  }


  @Autowired
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }
}
