package com.wl.xc.modelfun.tasks.file;

import com.wl.xc.modelfun.commons.enums.FileTaskType;
import java.util.Map;
import lombok.Data;

/**
 * 文件任务
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:18
 */
@Data
public class FileTask {

  /**
   * 任务类型
   */
  private FileTaskType type;
  /**
   * 文件路径
   */
  private String path;
  /**
   * 文件名称
   */
  private String fileName;
  /**
   * 任务ID
   */
  private Long taskId;

  /**
   * 本地文件路径
   */
  private String localPath;

  /**
   * 文件上传请求对应的唯一ID
   */
  private String requestId;

  private String createPeople;

  private Map<String, Object> config;

}
