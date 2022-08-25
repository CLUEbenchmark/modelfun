package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签对应的高频词
 *
 * @version 1.0
 * @date 2022/5/5 16:35
 */
@Data
@Schema(name = "标签对应的高频词")
public class LabelHFWordVO {

  /**
   * 标签id
   */
  @Schema(description = "标签id")
  private Integer labelId;
  /**
   * 标签描述
   */
  @Schema(description = "标签描述")
  private String labelDes;
  /**
   * 高频词
   */
  @Schema(description = "高频词")
  private String hfWord;
}
