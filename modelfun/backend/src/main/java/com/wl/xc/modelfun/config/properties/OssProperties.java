package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/4/13 11:46
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.oss")
public class OssProperties {

  private String endpoint;

  private String accessKeyId;

  private String accessKeySecret;

  private String bucketName;

  private String parentPath;

  private String stsEndpoint;

  private String roleArn;

  private int port = 9000;

}
