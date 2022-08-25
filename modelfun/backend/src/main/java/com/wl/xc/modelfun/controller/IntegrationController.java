package com.wl.xc.modelfun.controller;

import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskIntegrateKey;

import com.wl.xc.modelfun.commons.validation.group.Delete;
import com.wl.xc.modelfun.commons.validation.group.Update;
import com.wl.xc.modelfun.entities.req.AutoLabelResultReq;
import com.wl.xc.modelfun.entities.req.IntegrationReq;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.IntegrateOverviewVO;
import com.wl.xc.modelfun.entities.vo.IntegrationResultVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.IntegrationService;
import com.wl.xc.modelfun.service.TaskInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/12 10:42
 */
@Tag(name = "规则集成管理")
@Data
@Slf4j
@RestController
@RequestMapping("/integration")
public class IntegrationController {

  private IntegrationService integrationService;

  private StringRedisTemplate stringRedisTemplate;

  private TaskInfoService taskInfoService;

  @Deprecated
  @Operation(method = "POST", summary = "根据任务id分页查询规则集成结果")
  @PostMapping("/list")
  public PageResultVo<List<IntegrationResultVO>> getIntegrationPage(
      @Validated @RequestBody IntegrationReq req) {
    PageVO<IntegrationResultVO> pageVO = integrationService.getIntegrationPage(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Operation(method = "POST", summary = "集成规则")
  @PostMapping("/integrate")
  public ResultVo<Long> integrate(@Validated @RequestBody TaskIdReq req) {
    Long taskId = req.getTaskId();
    String key = getTaskIntegrateKey(taskId);
    Boolean aBoolean = stringRedisTemplate.opsForValue()
        .setIfAbsent(key, taskId.toString(), 1, TimeUnit.MINUTES);
    try {
      if (Boolean.FALSE.equals(aBoolean)) {
        return ResultVo.create("当前有集成任务正在进行中，请稍后再试", -1, false, null);
      }
      return integrationService.integrate(req);
    } finally {
      stringRedisTemplate.delete(key);
    }
  }

  @Operation(method = "GET", summary = "根据任务ID判断是否有集成任务正在运行")
  @GetMapping("/running/{taskId}")
  public ResultVo<Boolean> existRunningIntegration(@PathVariable("taskId") Long taskId) {
    return integrationService.existRunningIntegration(taskId);
  }

  @Operation(method = "POST", summary = "根据任务ID分页查询规则集成标签结果")
  @PostMapping("/label/result")
  public PageResultVo<List<DatasetInfoVO>> getIntegrationLabelPage(
      @Validated @RequestBody IntegrationReq req) {
    PageVO<DatasetInfoVO> pageVO = integrationService.getIntegrationLabelPage(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Operation(method = "POST", summary = "根据任务ID分页查询规则集成概览")
  @PostMapping("/overview")
  public ResultVo<IntegrateOverviewVO> getIntegrateOverview(@Validated @RequestBody TaskIdReq req) {
    return integrationService.getIntegrateOverview(req);
  }

  @Operation(method = "POST", summary = "开始自动标注")
  @PostMapping("/auto_label")
  public ResultVo<Long> autoLabel(@Validated @RequestBody TaskIdReq req) {
    return integrationService.autoLabel(req);
  }

  @Operation(method = "GET", summary = "根据任务ID判断是否有自动标注任务正在运行")
  @GetMapping("/labeling/{taskId}")
  public ResultVo<Boolean> existLabelingTask(@PathVariable("taskId") Long taskId) {
    return integrationService.existLabelingTask(taskId);
  }

  @Operation(method = "POST", summary = "修改自动标注的结果")
  @PostMapping("/edit/auto_label")
  public ResultVo<Boolean> editAutoLabelResult(
      @Validated({Update.class}) @RequestBody AutoLabelResultReq req) {
    return integrationService.editAutoLabelResult(req);
  }

  @Operation(method = "POST", summary = "删除自动标注的结果")
  @PostMapping("/del/auto_label")
  public ResultVo<Boolean> delAutoLabelResult(
      @Validated({Delete.class}) @RequestBody AutoLabelResultReq req) {
    return integrationService.delAutoLabelResult(req);
  }

  @Operation(method = "POST", summary = "NER自动标注")
  @PostMapping("/auto_label/ner")
  public ResultVo<Long> autoLabelNer(@Validated @RequestBody TaskIdReq req) {
    return integrationService.autoLabelNer(req);
  }

  @Autowired
  public void setIntegrationService(IntegrationService integrationService) {
    this.integrationService = integrationService;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }
}
