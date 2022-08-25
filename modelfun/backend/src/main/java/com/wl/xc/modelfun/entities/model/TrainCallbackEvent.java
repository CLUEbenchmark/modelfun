package com.wl.xc.modelfun.entities.model;

import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/11 14:41
 */
@Data
public class TrainCallbackEvent {

  private Long taskId;

  /**
   * 训练记录对应的ID
   */
  private Long recordId;

  /**
   * 延迟时间，单位：毫秒
   */
  private long delayMillisecond;

}
