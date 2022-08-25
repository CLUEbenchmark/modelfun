package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TextOneClickVO;
import com.wl.xc.modelfun.service.IntelligentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/5/26 11:21
 */
@Tag(name = "智能操作接口")
@RestController
@RequestMapping("/intelligent")
public class IntelligentController {

  private IntelligentService intelligentService;

  @Operation(method = "POST", summary = "一键标注")
  @PostMapping("/one_click_train")
  public ResultVo<Boolean> oneClickTrain(@Validated @RequestBody TaskIdReq taskIdReq) {
    return intelligentService.oneClickTrain(taskIdReq);
  }

  @Operation(method = "POST", summary = "文本一键标注")
  @PostMapping("/text_one_click")
  public ResultVo<Boolean> textOneClickTrain(@Validated @RequestBody TaskIdReq taskIdReq) {
    return intelligentService.textOneClickTrain(taskIdReq);
  }

  @Operation(method = "POST", summary = "是否存在文本一键标注任务")
  @PostMapping("/exist_text_click_task")
  public ResultVo<TextOneClickVO> existTextOneClick(@RequestBody TaskIdReq taskIdReq) {
    return intelligentService.existTextOneClick(taskIdReq);
  }

  @Autowired
  public void setIntelligentService(IntelligentService intelligentService) {
    this.intelligentService = intelligentService;
  }
}
