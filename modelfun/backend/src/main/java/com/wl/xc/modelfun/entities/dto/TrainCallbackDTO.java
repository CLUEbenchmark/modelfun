package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO.Arg;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/22 15:55
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrainCallbackDTO extends BaseCallbackDTO {

  /**
   * 准确率
   */
  @JsonProperty("accuracy")
  private Double accuracy;
  /**
   * 精确率
   */
  @JsonProperty("precision")
  private Double precision;
  /**
   * 召回率
   */
  @JsonProperty("recall")
  private Double recall;
  /**
   * f1
   */
  @JsonProperty("f1")
  private Double f1;

  @JsonProperty("report")
  private Map<String, Arg> report;
  /**
   * oss模型文件地址
   */
  @JsonProperty("url")
  private String url;
  /**
   * 训练完之后，对测试集的预测结果，即标签结果
   */
  @JsonProperty("preds")
  private List<Integer> preds;

  @JsonProperty("confusion_mx")
  private List<List<Integer>> confusionMx;
  /**
   * 状态，2-服务器内部错误，3-网络错误
   */
  private Integer status = 2;
}
