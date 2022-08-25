package com.wl.xc.modelfun.entities.vo;


import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.SUCCESS;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.commons.enums.ResponseCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 标准的返回，每个http的请求都以该对象返回，做到统一
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/4/13 14:47
 */
@JsonInclude
public class ResultVo<T> implements Serializable {

  private static final long serialVersionUID = -872079464102668214L;

  @Schema(description = "返回消息")
  private String msg;

  @Schema(description = "返回码")
  private Integer code;

  @Schema(description = "是否成功")
  private Boolean success;

  @Schema(description = "返回数据")
  private transient T data;

  public ResultVo() {
  }

  @JsonCreator(mode = Mode.PROPERTIES)
  public ResultVo(@JsonProperty("msg") String msg, @JsonProperty("code") Integer code,
      @JsonProperty("success") Boolean success, @JsonProperty("data") T data) {
    this.msg = msg;
    this.code = code;
    this.success = success;
    this.data = data;
  }

  public static <T> ResultVo<T> create(ResponseCodeEnum codeEnum, Boolean success, T data) {
    return new ResultVo<>(codeEnum.getMsg(), codeEnum.getCode(), success, data);
  }

  public static <T> ResultVo<T> create(ResponseCodeEnum codeEnum, String msg, Boolean success, T data) {
    return new ResultVo<>(msg, codeEnum.getCode(), success, data);
  }

  public static <T> ResultVo<T> create(String msg, Integer code, Boolean success, T data) {
    return new ResultVo<>(msg, code, success, data);
  }

  public static <T> ResultVo<T> createSuccess(T data, String msg) {
    return create(SUCCESS, msg, true, data);
  }

  public static <T> ResultVo<T> createSuccess(T data) {
    return create(SUCCESS, true, data);
  }


  public String getMsg() {
    return this.msg;
  }

  public ResultVo<T> setMsg(String msg) {
    this.msg = msg;
    return this;
  }

  public Integer getCode() {
    return this.code;
  }

  public ResultVo<T> setCode(Integer code) {
    this.code = code;
    return this;
  }

  public Boolean getSuccess() {
    return this.success;
  }

  public ResultVo<T> setSuccess(Boolean success) {
    this.success = success;
    return this;
  }

  public T getData() {
    return this.data;
  }

  public ResultVo<T> setData(T data) {
    this.data = data;
    return this;
  }

  @Override
  public String toString() {
    return "ResultVo{" +
        "msg=\"" + msg + '\"' +
        ", code=" + code +
        ", success=" + success +
        ", data=" + data +
        '}';
  }
}
