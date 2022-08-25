package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.model.OSSStsObject;
import com.wl.xc.modelfun.entities.req.DatasetUploadReq;
import com.wl.xc.modelfun.entities.req.ExpertFileParseReq;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * oss 服务类
 *
 * @version 1.0
 * @date 2022/4/13 11:43
 */
public interface OssService {

  boolean fileExit(String path);

  /**
   * 获取文件临时授权的签名URL
   *
   * @param path           文件路径
   * @param expireMillTime 过期时间,单位毫秒
   * @return 签名URL
   */
  String getUrlSigned(String path, long expireMillTime);

  /**
   * 获取oss临时访问凭证
   *
   * @param userPhone 用户手机号
   * @return oss临时访问凭证
   */
  OSSStsObject getStsToken(String userPhone);

  /**
   * 下载文件到本地
   *
   * @param filePath  文件路径
   * @param localPath 本地路径
   * @throws BusinessIOException 下载过程中的io异常
   */
  void download(String filePath, String localPath) throws BusinessIOException;

  /**
   * 流式下载文件
   *
   * @param filePath     文件路径
   * @param lineConsumer 文件行处理器
   * @throws BusinessIOException 下载过程中的io异常
   */
  void downloadStream(String filePath, Consumer<String> lineConsumer) throws BusinessIOException;

  void deleteFile(String path);

  /**
   * 删除多个文件
   *
   * @param paths 文件路径列表
   */
  void deleteFiles(List<String> paths);

  /**
   * 上传文件到oss
   *
   * @param fileList 文件列表
   */
  void uploadFiles(List<FileUpload> fileList);

  /**
   * 获取目录下的所有文件名称，不包含子目录
   *
   * @param dir 目录
   * @return 文件名称列表
   */
  List<String> listDirFiles(String dir);

  /**
   * 解析上传的文件
   *
   * @param req 上传请求
   * @return 解析任务ID
   */
  String parseDatasetUploadFile(DatasetUploadReq req);

  /**
   * 解析专家知识文件
   *
   * @param req 请求
   * @return 解析任务ID
   */
  String parseExpertUploadFile(ExpertFileParseReq req);

  /**
   * 请求解析ner数据文件
   *
   * @param req 请求
   * @return 解析任务ID
   */
  String parseNerUploadFile(DatasetUploadReq req);

  /**
   * 请求解析ner数据文件,真正的NER
   *
   * @param req 请求
   * @return 解析任务ID
   */
  String parseNerRUploadFile(DatasetUploadReq req);

  /**
   * 复制文件
   *
   * @param srcPath  源文件路径
   * @param destPath 目标文件路径
   */
  void copyFile(String srcPath, String destPath);

  /**
   * 上传文件到oss
   *
   * @param inputStream 文件流
   * @param path        上传文件路径
   */
  void uploadFile(InputStream inputStream, String path);
}
