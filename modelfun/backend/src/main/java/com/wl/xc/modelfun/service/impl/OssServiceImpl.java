package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.EXPERT_UPLOAD_TYPE;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.OSS_DELIMITER;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.OSS_TEMP_TAG;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import cn.hutool.core.lang.id.NanoId;
import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.config.properties.OssProperties;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.model.OSSStsObject;
import com.wl.xc.modelfun.entities.req.DatasetUploadReq;
import com.wl.xc.modelfun.entities.req.ExpertFileParseReq;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.FileTaskAppendEvent;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SetBucketLifecycleArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.UploadObjectArgs.Builder;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Expiration;
import io.minio.messages.Item;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import io.minio.messages.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/13 11:44
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService, SmartLifecycle, InitializingBean {

  private OssProperties ossProperties;

  private volatile boolean running = false;

  private volatile MinioClient ossClient;

  private StringRedisTemplate stringRedisTemplate;

  private FileUploadProperties fileUploadProperties;

  private ApplicationEventPublisher eventPublisher;

  @Override
  public boolean fileExit(String path) {
    MinioClient client = getOssClient();
    try {
      client.statObject(StatObjectArgs.builder().bucket(ossProperties.getBucketName()).object(path).build());
      return true;
    } catch (Exception e) {
      log.error("[OssServiceImpl.fileExit]", e);
      return false;
    }
  }

  @Override
  public String getUrlSigned(String path, long expireMillTime) {
    // 创建OSSClient实例。
    try {
      return getOssClient().getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(ossProperties.getBucketName())
              .object(path)
              .expiry((int) expireMillTime, TimeUnit.MILLISECONDS)
              .build());
    } catch (Exception e) {
      log.error("[OssServiceImpl.getUrlSigned] 获取文件临时授权的签名URL失败", e);
      throw new BusinessIllegalStateException("获取文件临时授权的签名URL失败");
    }
  }

  @Override
  public OSSStsObject getStsToken(String userPhone) {
    OSSStsObject object = new OSSStsObject();
    object.setBucketName(ossProperties.getBucketName());
    object.setEndpoint(ossProperties.getEndpoint());
    object.setAccessKeyId(ossProperties.getAccessKeyId());
    object.setAccessKeySecret(ossProperties.getAccessKeySecret());
    object.setPort(ossProperties.getPort());
    return object;
    // STS接入地址，例如sts.cn-hangzhou.aliyuncs.com。
    /*String endpoint = ossProperties.getStsEndpoint();
    // 填写步骤1生成的访问密钥AccessKey ID和AccessKey Secret。
    String AccessKeyId = ossProperties.getAccessKeyId();
    String accessKeySecret = ossProperties.getAccessKeySecret();
    // 填写步骤3获取的角色ARN。
    String roleArn = ossProperties.getRoleArn();
    // 以下Policy用于限制仅允许使用临时访问凭证向目标存储空间上传文件。
    DefaultAcsClient client = null;
    try {
      // regionId表示RAM的地域ID。以华东1（杭州）地域为例，regionID填写为cn-hangzhou。也可以保留默认值，默认值为空字符串（""）。
      String regionId = "cn-hangzhou";
      // 添加endpoint。适用于Java SDK 3.12.0及以上版本。
      DefaultProfile.addEndpoint(regionId, "Sts", endpoint);
      // 添加endpoint。适用于Java SDK 3.12.0以下版本。
      // DefaultProfile.addEndpoint("",regionId, "Sts", endpoint);
      // 构造default profile。
      IClientProfile profile = DefaultProfile.getProfile(regionId, AccessKeyId, accessKeySecret);
      // 构造client。
      client = new DefaultAcsClient(profile);
      final AssumeRoleRequest request = new AssumeRoleRequest();
      // 适用于Java SDK 3.12.0及以上版本。
      request.setSysMethod(MethodType.POST);
      // 适用于Java SDK 3.12.0以下版本。
      // request.setMethod(MethodType.POST);
      request.setRoleArn(roleArn);
      // 自定义角色会话名称，用来区分不同的令牌，例如可填写为SessionTest。
      request.setRoleSessionName(userPhone);
      //request.setPolicy(""); // 如果policy为空，则用户将获得该角色下所有权限。
      request.setDurationSeconds(3600L); // 设置临时访问凭证的有效时间为3600秒。
      final AssumeRoleResponse response = client.getAcsResponse(request);
      OSSStsObject stsObject = new OSSStsObject();
      stsObject.setEndpoint(ossProperties.getEndpoint());
      stsObject.setBucketName(ossProperties.getBucketName());
      stsObject.setAccessKeyId(response.getCredentials().getAccessKeyId());
      stsObject.setAccessKeySecret(response.getCredentials().getAccessKeySecret());
      stsObject.setSecurityToken(response.getCredentials().getSecurityToken());
      stsObject.setExpiration(response.getCredentials().getExpiration());
      return stsObject;
    } catch (ClientException | com.aliyuncs.exceptions.ClientException e) {
      log.error("[OssServiceImpl.getStsToken]", e);
      throw new BusinessIllegalStateException("获取STS临时授权凭证失败");
    } finally{
      if (client != null) {
        client.shutdown();
      }
    }*/
  }

  @Override
  public void download(String filePath, String localPath) throws BusinessIOException {
    MinioClient client = getOssClient();
    try {
      client.downloadObject(
          DownloadObjectArgs.builder()
              .bucket(ossProperties.getBucketName())
              .object(filePath)
              .filename(localPath)
              .build());
    } catch (ErrorResponseException e) {
      log.error(
          "[OssServiceImpl.download] oss接收到请求，但是服务器端出现异常. code:{}, msg:{}, requestId:{}, hostId:{}",
          e.errorResponse().code(), e.getMessage(), e.errorResponse().requestId(), e.errorResponse().hostId());
      throw new BusinessIOException("oss文件下载失败", e);
    } catch (Exception e) {
      log.error("[OssServiceImpl.download] 出现其他错误", e);
      throw new BusinessIOException("oss文件下载失败", e);
    }
  }

  @Override
  public void downloadStream(String filePath, Consumer<String> lineConsumer) throws BusinessIOException {
    try (InputStream stream =
        getOssClient()
            .getObject(
                GetObjectArgs.builder()
                    .bucket(ossProperties.getBucketName())
                    .object(filePath)
                    .build());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        lineConsumer.accept(line);
      }
    } catch (IOException | ServerException | InsufficientDataException | ErrorResponseException |
             NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
             InternalException e) {
      throw new BusinessIOException(e);
    }
  }

  @Override
  public void deleteFile(String path) {
    try {
      getOssClient()
          .removeObject(
              RemoveObjectArgs.builder()
                  .bucket(ossProperties.getBucketName())
                  .object(path)
                  .build());
    } catch (Exception e) {
      log.error("[OssServiceImpl.deleteFile]", e);
    }
  }

  @Override
  public void deleteFiles(List<String> paths) {
    if (paths == null || paths.isEmpty()) {
      return;
    }
    try {
      List<DeleteObject> objects = new LinkedList<>();
      for (String path : paths) {
        objects.add(new DeleteObject(path));
      }
      Iterable<Result<DeleteError>> results =
          getOssClient().removeObjects(
              RemoveObjectsArgs.builder().bucket(ossProperties.getBucketName()).objects(objects).build());
      boolean allSuccess = true;
      for (Result<DeleteError> result : results) {
        DeleteError error = result.get();
        log.error("Error in deleting object " + error.objectName() + "; " + error.message());
        allSuccess = false;
      }
      if (!allSuccess) {
        throw new BusinessIllegalStateException("oss文件删除失败");
      }
    } catch (Exception e) {
      log.error("[OssServiceImpl.deleteFile]", e);
    }
  }

  @Override
  public void uploadFiles(List<FileUpload> fileList) {
    try {
      for (FileUpload file : fileList) {
        Builder builder = UploadObjectArgs.builder()
            .bucket(ossProperties.getBucketName()).object(file.getDestPath())
            .filename(file.getFile().getAbsolutePath());
        if (StringUtils.isNotBlank(file.getContentType())) {
          builder.contentType(file.getContentType());
        }
        if (file.getTagMap() != null) {
          builder.tags(file.getTagMap());
        }
        getOssClient().uploadObject(builder.build());
        log.info("[OssServiceImpl.uploadFiles] 文件上传成功，文件名：{}，文件大小：{}", file.getDestPath(),
            file.getFile().length());
      }
    } catch (Exception e) {
      log.error("[OssServiceImpl.uploadFiles]", e);
      throw new BusinessIOException("oss文件上传失败", e);
    }
  }

  @Override
  public List<String> listDirFiles(String dir) {
    if (StringUtils.isBlank(dir)) {
      return Collections.emptyList();
    }
    if (!dir.endsWith(OSS_DELIMITER)) {
      dir = dir + OSS_DELIMITER;
    }
    MinioClient client = getOssClient();
    try {
      Iterable<Result<Item>> results = client.listObjects(
          ListObjectsArgs.builder()
              .bucket(ossProperties.getBucketName())
              .delimiter(OSS_DELIMITER)
              .prefix(dir)
              .build());
      List<String> fileList = new ArrayList<>();
      for (Result<Item> result : results) {
        Item item = result.get();
        if (item.isDir()) {
          continue;
        }
        fileList.add(item.objectName());
      }
      return fileList;
    } catch (Exception e) {
      log.error("[OssServiceImpl.listDirFiles]", e);
      throw new BusinessIOException("oss文件列表失败", e);
    }
  }

  @Override
  public String parseDatasetUploadFile(DatasetUploadReq req) {
    String requestId = NanoId.randomNanoId();
    // 提交oss文件上传任务
    String key = RedisKeyMethods.generateDatasetKey(req.getTaskId());
    // 默认保存一小时
    stringRedisTemplate.opsForValue().set(key, requestId, 1, TimeUnit.HOURS);
    stringRedisTemplate
        .opsForValue()
        .set(
            RedisKeyMethods.getFileTaskMsgKey(req.getTaskId(), requestId),
            "{\"success\": true, \"msg\": \"parsing\", \"complete\": false}",
            1,
            TimeUnit.HOURS);
    HashMap<String, Object> map = new HashMap<>();
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    map.put(SESSION_UID, userInfo.getUid());
    publishFileEvent(req.getPath(), req.getTaskId(), FileTaskType.DATASET, requestId, map);
    return requestId;
  }

  @Override
  public String parseExpertUploadFile(ExpertFileParseReq req) {
    String requestId = NanoId.randomNanoId();
    // 专家知识对应的redis key
    String key = RedisKeyMethods.generateExpertKey(req.getTaskId());
    // 默认保存一小时
    stringRedisTemplate.opsForValue().set(key, requestId, 1, TimeUnit.HOURS);
    stringRedisTemplate
        .opsForValue()
        .set(
            RedisKeyMethods.getFileTaskMsgKey(req.getTaskId(), requestId),
            "{\"success\": true, \"msg\": \"parsing\", \"complete\": false}",
            1,
            TimeUnit.HOURS);
    Map<String, Object> config = new HashMap<>();
    config.put(EXPERT_UPLOAD_TYPE, req.getUploadType());
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    config.put(SESSION_UID, userInfo.getUid());
    publishFileEvent(req.getPath(), req.getTaskId(), FileTaskType.EXPERT, requestId, config);
    return requestId;
  }

  @Override
  public String parseNerUploadFile(DatasetUploadReq req) {
    String requestId = NanoId.randomNanoId();
    // 提交oss文件上传任务
    HashMap<String, Object> map = new HashMap<>();
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    map.put(SESSION_UID, userInfo.getUid());
    publishFileEvent(req.getPath(), req.getTaskId(), FileTaskType.NER, requestId, map);
    return requestId;
  }

  @Override
  public String parseNerRUploadFile(DatasetUploadReq req) {
    String requestId = NanoId.randomNanoId();
    // 提交oss文件上传任务
    String key = RedisKeyMethods.generateDatasetKey(req.getTaskId());
    // 默认保存一小时
    stringRedisTemplate.opsForValue().set(key, requestId, 1, TimeUnit.HOURS);
    // 提交oss文件上传任务
    HashMap<String, Object> map = new HashMap<>();
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    map.put(SESSION_UID, userInfo.getUid());
    map.put("fileType", req.getFileType());
    publishFileEvent(req.getPath(), req.getTaskId(), FileTaskType.NER_R, requestId, map);
    return requestId;
  }

  @Override
  public void copyFile(String srcPath, String destPath) {
    MinioClient client = getOssClient();
    try {
      client.copyObject(
          CopyObjectArgs.builder()
              .bucket(ossProperties.getBucketName())
              .object(destPath)
              .source(
                  CopySource.builder()
                      .bucket(ossProperties.getBucketName())
                      .object(srcPath)
                      .build())
              .build());
    } catch (ErrorResponseException
             | InsufficientDataException
             | InternalException
             | InvalidKeyException
             | InvalidResponseException
             | IOException
             | NoSuchAlgorithmException
             | ServerException
             | XmlParserException e) {
      log.error("[OssServiceImpl.copyFile]", e);
      throw new BusinessIOException("oss文件复制失败", e);
    }
  }

  @Override
  public void uploadFile(InputStream inputStream, String path) {
    try {
      getOssClient()
          .putObject(
              PutObjectArgs.builder()
                  .bucket(ossProperties.getBucketName())
                  .object(path)
                  .stream(inputStream, inputStream.available(), -1)
                  .contentType("application/octet-stream")
                  .build());
    } catch (Exception e) {
      throw new BusinessIOException("oss文件上传失败", e);
    }
  }

  private void publishFileEvent(String path, Long taskId, FileTaskType type, String requestId,
      Map<String, Object> config) {
    FileTask fileTask = new FileTask();
    fileTask.setRequestId(requestId);
    fileTask.setType(type);
    path = path.replace("\\", "/");
    String[] split = path.split("/");
    fileTask.setPath(path);
    fileTask.setFileName(split[split.length - 1]);
    fileTask.setLocalPath(
        Paths.get(fileUploadProperties.getTempPath(), requestId, fileTask.getFileName())
            .toString());
    fileTask.setTaskId(taskId);
    fileTask.setCreatePeople(ServletUserHolder.getUserByContext().getUserId().toString());
    fileTask.setConfig(config);
    eventPublisher.publishEvent(new FileTaskAppendEvent(fileTask));
  }

  private MinioClient getOssClient() {
    if (ossClient == null) {
      synchronized (this) {
        if (ossClient == null) {
          ossClient = initOSSClient();
        }
      }
    }
    return ossClient;
  }

  private MinioClient initOSSClient() {
    if (ossClient == null) {
      ossClient =
          MinioClient.builder()
              .endpoint(ossProperties.getEndpoint(), 9000, false)
              .credentials(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret())
              .build();
    }
    running = true;
    return ossClient;
  }

  /**
   * 通过文件名判断并获取OSS服务文件上传时文件的contentType
   *
   * @param fileName 文件名
   * @return 文件的contentType
   */
  public String getContentType(String fileName) {
    String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
    if ("png".equalsIgnoreCase(fileExtension)) {
      return "image/png";
    }
    if ("bmp".equalsIgnoreCase(fileExtension)) {
      return "image/bmp";
    }
    if ("gif".equalsIgnoreCase(fileExtension)) {
      return "image/gif";
    }
    if ("jpeg".equalsIgnoreCase(fileExtension) || "jpg".equalsIgnoreCase(fileExtension)) {
      return "image/jpeg";
    }
    if ("html".equalsIgnoreCase(fileExtension)) {
      return "text/html";
    }
    if ("txt".equalsIgnoreCase(fileExtension)) {
      return "text/plain";
    }
    if ("vsd".equalsIgnoreCase(fileExtension)) {
      return "application/vnd.visio";
    }
    if ("ppt".equalsIgnoreCase(fileExtension) || "pptx".equalsIgnoreCase(fileExtension)) {
      return "application/vnd.ms-powerpoint";
    }
    if ("doc".equalsIgnoreCase(fileExtension) || "docx".equalsIgnoreCase(fileExtension)) {
      return "application/msword";
    }
    if ("xml".equalsIgnoreCase(fileExtension)) {
      return "text/xml";
    }
    if ("zip".equalsIgnoreCase(fileExtension)) {
      return "application/zip";
    }
    return "text/html";
  }

  @Autowired
  public void setOssProperties(OssProperties ossProperties) {
    this.ossProperties = ossProperties;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
    if (ossClient != null) {
      ossClient = null;
    }
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    initOSSClient();
    boolean found = ossClient.bucketExists(BucketExistsArgs.builder().bucket(ossProperties.getBucketName()).build());
    if (!found) {
      ossClient.makeBucket(MakeBucketArgs.builder().bucket(ossProperties.getBucketName()).build());
      log.info("[OssServiceImpl.afterPropertiesSet] 创建bucket: {}", ossProperties.getBucketName());
      // 创建失效规则
      List<LifecycleRule> rules = new LinkedList<>();
      rules.add(
          new LifecycleRule(
              Status.ENABLED,
              null,
              new Expiration((ZonedDateTime) null, 30, null),
              new RuleFilter(new Tag(OSS_TEMP_TAG, "30")),
              "rule1",
              null,
              null,
              null));
      LifecycleConfiguration config = new LifecycleConfiguration(rules);
      ossClient.setBucketLifecycle(
          SetBucketLifecycleArgs.builder().bucket(ossProperties.getBucketName()).config(config).build());
      log.info("[OssServiceImpl.afterPropertiesSet] 创建bucket: {} 的失效规则", ossProperties.getBucketName());
    } else {
      log.debug("[OssServiceImpl.afterPropertiesSet] bucket:modelfun already exists");
    }
  }
}
