package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 专家知识上传文件保存请求
 *
 * @version 1.0
 * @date 2022/4/14 16:37
 */
@Data
@Schema(description = "专家知识上传文件保存请求")
public class ExpertFileParseReq {

  /**
   * 文件的完整路径，不包括bucketName
   */
  @Schema(description = "文件的完整路径，不包括bucketName", name = "path", example = "upload/1/expert/专家知识1.zip")
  @NotBlank(message = "文件路径不能为空")
  private String path;

  /**
   * 任务ID
   */
  @NotNull(message = "任务ID不能为空")
  @Schema(description = "任务ID", name = "taskId", example = "1")
  private Long taskId;

  /**
   * 文件上传类型，1：覆盖，2：追加
   */
  @NotNull(message = "文件上传类型不能为空")
  @Schema(description = "文件上传类型，1：覆盖，2：追加", name = "uploadType", example = "1")
  private Integer uploadType;

}
