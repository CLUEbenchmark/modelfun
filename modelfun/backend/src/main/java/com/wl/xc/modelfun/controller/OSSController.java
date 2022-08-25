package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.commons.enums.ResponseCodeEnum;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.model.OSSStsObject;
import com.wl.xc.modelfun.entities.req.DatasetUploadReq;
import com.wl.xc.modelfun.entities.req.ExpertFileParseReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用于处理OSS请求的controller
 *
 * @version 1.0
 * @date 2022/4/13 13:48
 */
@Tag(name = "oss管理")
@Slf4j
@RestController
@RequestMapping("/oss")
public class OSSController {

  private OssService ossService;

  private FileUploadProperties fileUploadProperties;

  /**
   * 获取OSS临时凭证
   *
   * @return OSS临时凭证
   */
  @Operation(method = "GET", summary = "获取OSS临时凭证")
  @GetMapping("/sts")
  public ResultVo<OSSStsObject> getOSSToken() {
    OSSStsObject stsToken;
    try {
      LoginUserInfo user = ServletUserHolder.getUserByContext();
      stsToken = ossService.getStsToken(user.getUserPhone());
    } catch (Exception e) {
      return ResultVo.create(ResponseCodeEnum.INTERNAL_EXCEPTION, false, null);
    }
    return ResultVo.createSuccess(stsToken);
  }

  /**
   * 根据文件路径获取签名URL
   *
   * @param filePath 文件路径
   * @return 签名URL
   */
  @Operation(method = "GET", summary = "根据文件路径获取签名URL")
  @GetMapping("/get_file_url")
  public ResultVo<String> getUrlSigned(@RequestParam("filePath") String filePath) {
    try {
      String urlSigned = ossService.getUrlSigned(filePath, 30 * 60 * 1000);
      return ResultVo.createSuccess(urlSigned);
    } catch (Exception e) {
      return ResultVo.create(ResponseCodeEnum.INTERNAL_EXCEPTION, false, null);
    }
  }

  @Operation(method = "POST", summary = "数据集文件上传解析请求，返回的是一个解析的任务ID")
  @PostMapping("/upload/dataset")
  public ResultVo<String> parseUploadFile(@Validated @RequestBody DatasetUploadReq req) {
    String parseTaskId = ossService.parseDatasetUploadFile(req);
    return ResultVo.createSuccess(parseTaskId);
  }

  @Operation(method = "POST", summary = "专家知识上传解析请求，返回的是一个解析的任务ID")
  @PostMapping("/upload/expert")
  public ResultVo<String> parseExpertUploadFile(@Validated @RequestBody ExpertFileParseReq req) {
    String parseTaskId = ossService.parseExpertUploadFile(req);
    return ResultVo.createSuccess(parseTaskId);
  }

  @Operation(method = "POST", summary = "ner数据集文件上传解析请求，返回的是一个解析的任务ID")
  @PostMapping("/upload/ner")
  public ResultVo<String> parseNerUploadFile(@Validated @RequestBody DatasetUploadReq req) {
    String parseTaskId = ossService.parseNerUploadFile(req);
    return ResultVo.createSuccess(parseTaskId);
  }

  @Operation(method = "POST", summary = "ner数据集文件上传解析请求，返回的是一个解析的任务ID")
  @PostMapping("/upload/nerr")
  public ResultVo<String> parseNerRUploadFile(@Validated @RequestBody DatasetUploadReq req) {
    Integer fileType = req.getFileType();
    if (fileType == null) {
      return ResultVo.create("请选择正确的文件类型", -1, false, null);
    }
    if (fileType != 1 && fileType != 2 && fileType != 3) {
      return ResultVo.create("请选择正确的文件类型", -1, false, null);
    }
    String parseTaskId = ossService.parseNerRUploadFile(req);
    return ResultVo.createSuccess(parseTaskId);
  }


  @PostMapping("/upload")
  public ResultVo<String> uploadFile(MultipartFile file, String type, Integer taskId) {
    if (file == null) {
      return ResultVo.create("文件为空", -1, false, null);
    }
    String filename = file.getOriginalFilename();
    if (filename == null || filename.isEmpty()) {
      return ResultVo.create("文件名为空", -1, false, null);
    }
    if (taskId == null || taskId < 1) {
      return ResultVo.create("任务ID为空", -1, false, null);
    }
    if (type == null || type.isEmpty()) {
      return ResultVo.create("文件类型为空", -1, false, null);
    }
    String path = fileUploadProperties.getOssPrefix() + taskId + "/" + type + "/" + filename;
    try {
      ossService.uploadFile(file.getInputStream(), path);
      return ResultVo.createSuccess(path);
    } catch (Exception e) {
      log.error("[OSSController.uploadFile]", e);
      return ResultVo.create(ResponseCodeEnum.INTERNAL_EXCEPTION, false, null);
    }
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }
}
