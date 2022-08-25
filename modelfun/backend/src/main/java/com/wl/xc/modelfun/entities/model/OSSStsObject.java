package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * oss的sts对象
 *
 * @version 1.0
 * @date 2022/4/13 12:45
 */
@Data
@Schema(name = "oss的sts对象", description = "oss的sts对象")
public class OSSStsObject {

  @Schema(name = "endpoint", description = "oss地址")
  private String endpoint;

  @Schema(name = "bucketName", description = "oss的bucketName")
  @JsonProperty("bucket")
  private String bucketName;

  @Schema(name = "accessKeyId", description = "临时访问密钥")
  @JsonProperty("accessKey")
  private String accessKeyId;

  @Schema(name = "accessKeySecret", description = "临时访问密钥")
  @JsonProperty("secretKey")
  private String accessKeySecret;

  @Schema(name = "securityToken", description = "从STS服务获取的安全令牌")
  private String securityToken;

  @JsonProperty("port")
  private Integer port = 9000;

  /**
   * 过期时间
   */
  @Schema(name = "expiration", description = "过期时间")
  private String expiration;

}
