package com.wl.xc.modelfun.entities.req;

import com.wl.xc.modelfun.commons.enums.ModelType;
import com.wl.xc.modelfun.commons.validation.EnumValidator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 模型训练请求的实体类
 *
 * @version 1.0
 * @date 2022/5/6 9:36
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class TrainReq extends TaskIdReq {

  @EnumValidator(value = ModelType.class, method = "getType", message = "模型类型不存在，请检查模型类型是否正确")
  private Integer model;

}
