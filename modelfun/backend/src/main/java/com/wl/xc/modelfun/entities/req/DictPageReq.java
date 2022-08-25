package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/12 16:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "DictPageReq", description = "字典分页查询请求")
public class DictPageReq extends BasePageQueryDTO {

  @Schema(name = "id", description = "记录ID")
  private Integer id;

  /**
   * 字典映射的key
   */
  @Schema(name = "mapKey", description = "字典映射的key")
  private String mapKey;

  /**
   * 字典映射的value
   */
  @Schema(name = "mapValue", description = "字典映射的value")
  private String mapValue;

  /**
   * 字典映射分组
   */
  @Schema(name = "mapGroup", description = "字典映射分组")
  private String mapGroup;

  /**
   * 描述
   */
  @Schema(name = "mapDesc", description = "描述")
  private String mapDesc;

  /**
   * 排序
   */
  @Schema(name = "mapSort", description = "排序")
  private Integer mapSort;
}
