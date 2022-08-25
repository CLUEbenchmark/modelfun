package com.wl.xc.modelfun.entities.dto;

import lombok.Data;

/**
 * 用于websocket传输的数据
 *
 * @version 1.0
 * @date 2022/5/13 13:19
 */
@Data
public class WebsocketDataDTO {

  /**
   * 任务ID
   */
  private Long taskId;
  /**
   * 任务名称
   */
  private String taskName;
  /**
   * 消息
   */
  private String msg;
  /**
   * 是否成功
   */
  private boolean success;

  private int state = 1;

  public WebsocketDataDTO() {
  }

  public WebsocketDataDTO(Long taskId, String taskName, String msg, boolean success) {
    this.taskId = taskId;
    this.taskName = taskName;
    this.msg = msg;
    this.success = success;
  }


  public static WebsocketDataDTO create(Long taskId, String taskName, String msg, boolean success) {
    return new WebsocketDataDTO(taskId, taskName, msg, success);
  }

}
