package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/4/15 15:30
 */
@NoArgsConstructor
@Data
public class OpenApiResponse {


  /**
   * 0表示调用成功，非0表示调用失败
   */
  @JsonProperty("code")
  private Integer code;
  /**
   * 如果调用失败，返回失败原因
   */
  @JsonProperty("msg")
  private String msg;
  /**
   * data
   */
  @JsonProperty("data")
  private List<DataDTO> data;

  /**
   * Item
   */
  @NoArgsConstructor
  @Data
  public static class DataDTO {

    /**
     * 语料内容
     */
    @JsonProperty("sentence")
    private String sentence;
    /**
     * 标签ID
     */
    @JsonProperty("labelId")
    @JsonAlias({"label"})
    private Integer labelId;
    /**
     * 标签描述
     */
    @JsonProperty("labelDes")
    private String labelDes;
  }
}
