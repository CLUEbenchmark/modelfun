package com.wl.xc.modelfun.entities.vo;

import lombok.Data;

/**
 * 任务进度
 *
 * @version 1.0
 * @date 2022/4/13 17:30
 */
@Data

public class TaskProgressVO {


  /**
   * 任务是否已经完成
   */
  private Boolean complete;
  /**
   * 任务是否成功
   */
  private Boolean success;

  /**
   * 任务消息，如果正确完成，则为success，否则为错误信息
   */
  private String msg;
}
