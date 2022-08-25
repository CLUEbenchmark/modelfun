package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/24 14:48
 */
@Data
@TableName(value = "mf_ner_test_data")
public class NerTestDataPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField(value = "task_id")
  private Long taskId;

  @TableField(value = "data_id")
  private Long dataId;

  @TableField(value = "sentence")
  private String sentence;

  @TableField(value = "relations")
  private String relations;

  @TableField(value = "show_data")
  private Integer showData;

  @TableField(value = "data_type")
  private Integer dataType;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATA_ID = "data_id";

  public static final String COL_SENTENCE = "sentence";

  public static final String COL_RELATIONS = "relations";

  public static final String COL_SHOW_DATA = "show_data";

  public static final String COL_DATA_TYPE = "data_type";
}