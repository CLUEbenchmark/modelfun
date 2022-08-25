package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据集中测试集数据的明细，每条记录对应一条数据
 *
 * @version 1.0
 * @date 2022/4/11 18:56
 */
@Data
@TableName(value = "mf_test_data")
public class TestDataPO {

  /**
   * 自增ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 数据集ID
   */
  @TableField(value = "data_set_id")
  private Integer dataSetId;

  /**
   * 数据ID
   */
  @TableField(value = "data_id")
  private Long dataId;

  /**
   * 标签
   */
  @TableField(value = "`label`")
  private Integer label;

  /**
   * 语料内容
   */
  @TableField(value = "sentence")
  private String sentence;

  /**
   * 标签内容
   */
  @TableField(value = "label_des")
  private String labelDes;

  /**
   * 是否展示0：否 1：是
   */
  @TableField(value = "show_data")
  private Integer showData;

  /**
   * 数据类型：1-测试集全集；4:-验证集；5-测试集；8：训练集
   */
  @TableField(value = "data_type")
  private Integer dataType;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATA_SET_ID = "data_set_id";

  public static final String COL_DATA_ID = "data_id";

  public static final String COL_LABEL = "label";

  public static final String COL_SENTENCE = "sentence";

  public static final String COL_LABEL_DES = "label_des";

  public static final String COL_SHOW = "show_data";

  public static final String COL_DATA_TYPE = "data_type";
}