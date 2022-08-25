package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 10:51
 */
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.file.upload")
@Data
public class FileUploadProperties {

  /**
   * 上传文件存到oss之前的临时目录
   */
  private String tempPath;
  /**
   * 上传文件到oss以及保存到数据库的最大并行数，默认50
   */
  private Integer maxParallelTask = 50;
  /**
   * 上传文件到oss后的前缀路径，不同环境需要设置不同的前缀路径
   */
  private String ossPrefix;
  /**
   * 一些临时文件的路径
   */
  private String ossTempPath;
  /**
   * 文件地址的失效时间，单位为毫秒，默认1800秒
   */
  private long expireTime = 1800 * 1000;
  /**
   * ner测试集的切分率，从0到100
   */
  private int nerSliceRate = 0;
}
