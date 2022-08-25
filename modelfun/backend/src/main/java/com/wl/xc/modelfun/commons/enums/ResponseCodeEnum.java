package com.wl.xc.modelfun.commons.enums;

/**
 * 业务异常码
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/4/28 16:37
 */
public enum ResponseCodeEnum {
  /**
   * 成功代码
   */
  SUCCESS(0, "success"),
  /**
   * http成功
   */
  HTTP_SUCCESS(200, "success"),
  /**
   * 异常码，http相关
   */
  FORBIDDEN(403, "禁止访问"),
  NOT_FOUND(404, "资源不存在"),
  REQUIRE_ACCESS(407, "要求身份验证"),
  INTERNAL_EXCEPTION(500, "服务器内部异常"),

  /**
   * 业务异常码
   */
  TOO_MANY_REQUEST(1, "请求次数过多"),
  NO_SIGN(2, "缺少签名参数"),
  INVALID_SIGN(3, "无效签名"),
  NO_APP_ID(4, "缺少app_id参数"),
  INVALID_APP_ID(5, "无效的app_id参数"),
  NO_TIMESTAMP(6, "缺少时间戳参数"),
  INVALID_TIMESTAMP(7, "非法的时间戳参数"),
  ILLEGAL_PARAMETER(8, "非法参数"),
  NOT_EXIST(9, "数据不存在"),

  /**
   * 规则相关业务异常码
   */
  RULE_NOT_EXIST(1001, "规则不存在"),
  RULE_DELETE_FAIL(1002, "规则删除失败"),
  RULE_ADD_FAIL(1003, "规则创建失败"),
  RULE_TYPE_NOT_EXIST(1004, "规则类型不存在"),
  RULE_UPDATE_FAIL(1005, "规则更新失败"),
  /**
   * 集成相关业务异常码
   */
  DATASET_NOT_EXIT(2001, "当前任务下不存在数据集，无法集成"),
  INTEGRATION_RUNNING(2002, "集成任务正在运行中，请稍后再试"),
  RULE_RUNNING(2003, "有规则正在运行中，或者运行失败，请检查规则"),
  NOT_MODIFY(2004, "规则或数据未做任何修改，无需集成"),
  RULE_NOT_EXIT(2005, "任务下不存在规则，无法集成"),
  RULE_NOT_ENOUGH(2006, "规则数量不足3个，无法集成"),
  RULE_FAILED(2007, "规则运行失败！"),
  /**
   * 数据训练相关业务异常码
   */
  INTEGRATION_NOT_EXIT(3001, "尚未完成集成，无法训练"),
  TRAIN_RUNNING(3002, "正在训练中，请稍后再试"),
  TRAIN_NOT_MODIFY(3003, "训练数据集未更新，不支持再次训练"),
  /**
   * 自动标注相关业务异常码
   */
  NOT_INTEGRATION(4001, "不存在集成记录，请先集成！"),
  WAIT_INTEGRATION(4002, "正在集成中，请稍后再试！"),
  INTEGRATION_FAILED(4003, "最近一次集成任务失败，请修改规则后重新集成！"),
  AUTO_LABEL_FAILED(4004, "自动标注失败，请重新标注！"),
  AUTO_LABEL_RUNNING(4005, "正在自动标注中，请稍后再试"),
  NOT_AUTO_LABELED(4006, "尚未进行自动标注，请先进行自动标注！"),
  ;


  private final int code;
  private final String msg;

  ResponseCodeEnum(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public int getCode() {
    return this.code;
  }

  public String getMsg() {
    return this.msg;
  }
}
