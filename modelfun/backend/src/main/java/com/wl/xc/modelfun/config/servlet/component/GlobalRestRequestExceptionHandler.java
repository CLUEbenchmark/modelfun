package com.wl.xc.modelfun.config.servlet.component;


import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.INTERNAL_EXCEPTION;

import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/4/14 11:03
 */
@RestControllerAdvice
public class GlobalRestRequestExceptionHandler {

  private static final String EX_MSG = "服务内部错误！";

  private static final Logger log = LoggerFactory.getLogger(GlobalRestRequestExceptionHandler.class);


  @ExceptionHandler(value = BusinessException.class)
  public ResultVo<?> businessExceptionHandler(HttpServletRequest request, BusinessException exception) {
    log.error("[GlobalRestRequestExceptionHandler.businessExceptionHandler] url is {}",
        request.getRequestURI(), exception);
    return new ResultVo<>(exception.getMessage(), exception.getCode(), false, null);
  }

  /**
   * 参数校验错误
   *
   * @param request   请求
   * @param exception 异常
   * @return 通用返回
   */
  @ExceptionHandler(value = BindException.class)
  public ResultVo<?> methodArgumentNotValidException(HttpServletRequest request, BindException exception) {
    BindingResult result = exception.getBindingResult();
    String message = null;
    //组装校验错误信息
    if (result.hasErrors()) {
      List<ObjectError> list = result.getAllErrors();
      StringBuilder errorMsgBuffer = new StringBuilder();
      for (ObjectError error : list) {
        if (error instanceof FieldError) {
          FieldError errorMessage = (FieldError) error;
          errorMsgBuffer.append(errorMessage.getDefaultMessage()).append(",");
        }
      }
      //返回信息格式处理
      message = errorMsgBuffer.substring(0, errorMsgBuffer.length() - 1);
    }
    if (message == null || message.isEmpty()) {
      message = EX_MSG;
    }
    log.error("[GlobalRestRequestExceptionHandler.methodArgumentNotValidException] url: {}, msg: {}",
        request.getRequestURI(), message);
    return new ResultVo<>(message, 500, false, null);
  }

  @ExceptionHandler(value = Exception.class)
  public ResultVo<?> exceptionHandler(HttpServletRequest request, Exception exception) {
    log.error("[GlobalRestRequestExceptionHandler.exceptionHandler] url: {}",
        request.getRequestURI(), exception);
    return ResultVo.create(INTERNAL_EXCEPTION, false, null);
  }

}
