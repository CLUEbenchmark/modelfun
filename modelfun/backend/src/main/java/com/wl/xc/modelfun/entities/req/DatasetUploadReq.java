package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文件上传请求
 *
 * @version 1.0
 * @date 2022/4/13 14:29
 */
@Data
@Schema(description = "文件上传请求", name = "FileUploadReq")
public class DatasetUploadReq {

  /**
   * 文件的完整路径，不包括bucketName
   */
  @Schema(description = "文件的完整路径，不包括bucketName", name = "path", example = "dataset/数据集1.zip")
  @NotBlank(message = "文件路径不能为空")
  private String path;

  /**
   * 任务ID
   */
  @NotNull(message = "任务ID不能为空")
  @Schema(description = "任务ID", name = "taskId", example = "1")
  private Long taskId;

  @Schema(description = "文件类型", name = "fileType", example = "1")
  private Integer fileType;

}
