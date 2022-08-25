package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.entities.req.UserFeedbackReq;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/7/11 18:32
 */
@Tag(name = "用户反馈")
@Slf4j
@RestController
@RequestMapping("/feedback")
public class FeedbackController {


  @Operation(method = "POST", summary = "查询问题反馈")
  @PostMapping("/query")
  public PageResultVo<List<Boolean>> getFeedbackPage(@Validated @RequestBody UserFeedbackReq req) {
    return PageResultVo.createSuccess(null);
  }

  @Operation(method = "POST", summary = "新增问题反馈")
  @PostMapping("/add")
  public ResultVo<Boolean> addFeedback(@Validated @RequestBody UserFeedbackReq req) {
    return ResultVo.createSuccess(true);
  }

  @Operation(method = "POST", summary = "更新问题反馈")
  @PostMapping("/update")
  public ResultVo<Boolean> updateFeedback(@Validated @RequestBody UserFeedbackReq req) {
    return ResultVo.createSuccess(true);
  }

  @Operation(method = "POST", summary = "删除问题反馈")
  @PostMapping("/del")
  public ResultVo<Boolean> deleteFeedback(@Validated @RequestBody UserFeedbackReq req) {
    return ResultVo.createSuccess(true);
  }


}
