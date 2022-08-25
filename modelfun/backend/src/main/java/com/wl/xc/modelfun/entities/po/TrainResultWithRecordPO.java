package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 模型训练结果表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrainResultWithRecordPO extends TrainResultPO {

  @TableField("data_version")
  private String dataVersion;

  @TableField("train_file")
  private String trainFile;

  @TableField("model_type")
  private Integer modelType;

  @TableField("train_status")
  private Integer trainStatus;

  @TableField(value = "label_array")
  private String labelArray;
}
