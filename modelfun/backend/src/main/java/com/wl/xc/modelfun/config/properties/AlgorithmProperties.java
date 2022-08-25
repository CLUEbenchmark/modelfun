package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/4/19 16:20
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.algo")
public class AlgorithmProperties {

  /**
   * 每次调用GPT3接口的最大批次数量，默认为1000
   */
  private int gptBatchSize = 1000;
  /**
   * gpt3接口的地址
   */
  private String gpt3Url;
  /**
   * gpt3接口的测试地址
   */
  private String gptTestUrl;
  /**
   * GPT key
   */
  private String gptKey;
  /**
   * gpt3接口的超时时间，单位为秒，默认1800秒（半小时）
   */
  private long gpt3Timeout = 60 * 10;
  /**
   * 模型训练地址
   */
  private String trainPath;
  /**
   * 规则集成路径
   */
  private String integratePath;
  /**
   * label function路径
   */
  private String labelFunctionPath;
  /**
   * label function 测试路径
   */
  private String labelFunctionTestPath;
  /**
   * 高频词汇路径
   */
  private String keywordTopPath;
  /**
   * 自动标注路径
   */
  private String autoLabelPath;
  /**
   * ner一键标注
   */
  private String nerOneClickUrl;

  private String genLfUrl;

  private String nerTrainUrl;

  private String nerAutoLabelUrl;

  /**
   * 文本小样本学习接口地址
   */
  private String fewShotUrl;
  /**
   * 算法异步回调的地址
   */
  private String algorithmCallbackUrl;
}
