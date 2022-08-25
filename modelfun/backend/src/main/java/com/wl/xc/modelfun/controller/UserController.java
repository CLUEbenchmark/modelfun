package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.entities.req.LoginReq;
import com.wl.xc.modelfun.entities.vo.LoginSuccessVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/12 13:13
 */
@Tag(name = "用户管理")
@RestController
public class UserController {

  private UserService userService;


  @Operation(method = "POST", summary = "用户登录")
  @PostMapping("/doLogin")
  public ResultVo<LoginSuccessVO> getUser(@RequestBody LoginReq req) {
    return ResultVo.createSuccess(new LoginSuccessVO());
  }

  @Operation(method = "GET", summary = "登出")
  @GetMapping("/doLogout")
  public ResultVo<String> logout() {
    return ResultVo.createSuccess("登出成功");
  }

  @Operation(method = "POST", summary = "注册")
  @GetMapping("/register")
  public ResultVo<Boolean> register(@Validated @RequestBody LoginReq req) {
    return userService.register(req);
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
}
