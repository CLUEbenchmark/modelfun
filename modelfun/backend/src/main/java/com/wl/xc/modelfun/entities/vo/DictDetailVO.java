package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/12 15:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class DictDetailVO extends DictKeyValueVO {

  @Schema(name = "id", description = "记录ID")
  private Integer id;

  @Schema(name = "mapGroup", description = "字典分组")
  private String mapGroup;

  @Schema(name = "mapDesc", description = "字典描述")
  private String mapDesc;

  /**
   * 创建时间
   */
  @Schema(name = "createDatetime", description = "创建时间", example = "2020-04-12 09:48:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDatetime;

  /**
   * 创建人
   */
  @Schema(name = "createPeople", description = "创建人")
  private String createPeople;

  /**
   * 更新时间
   */
  @Schema(name = "updateDatetime", description = "更新时间", example = "2020-04-12 09:48:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateDatetime;

  /**
   * 更新人
   */
  @Schema(name = "updatePeople", description = "更新人")
  private String updatePeople;

}
