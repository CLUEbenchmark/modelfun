package com.wl.xc.modelfun.tasks.file.handlers;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ZipUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.model.DataSetParseResult;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.FileTaskHandler;
import com.wl.xc.modelfun.tasks.rule.RuleTaskAppendEventPublisher;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/2 15:03
 */
@Slf4j
public abstract class AbstractFileUploadTaskHandler implements FileTaskHandler {

  protected StringRedisTemplate stringRedisTemplate;

  protected ObjectMapper objectMapper;
  protected DatasetInfoService datasetInfoService;

  protected OssService ossService;

  protected RuleInfoService ruleInfoService;

  protected RuleResultService ruleResultService;

  protected RuleUnlabeledResultService ruleUnlabeledResultService;

  protected TaskInfoService taskInfoService;

  protected RuleTaskAppendEventPublisher publisher;

  protected FileUploadProperties fileUploadProperties;

  @Override
  public void handle(FileTask fileTask) {
    String requestId = fileTask.getRequestId();
    String localPath = fileTask.getLocalPath();
    File parent = Paths.get(localPath).getParent().toFile();
    try {
      if (!parent.exists()) {
        boolean createResult = parent.mkdirs();
        if (!createResult) {
          throw new BusinessIllegalStateException("????????????????????????");
        }
      }
      // ????????????????????????
      if (!checkFileExist(fileTask)) {
        throw new BusinessIllegalStateException("??????????????????????????????");
      }

      // ???oss?????????????????????
      ossService.download(fileTask.getPath(), localPath);
      // ?????????????????????
      internalHandle(fileTask);
      // ????????????
      handleTaskSuccess(requestId, fileTask);
    } catch (BusinessException e) {
      handleTaskError(requestId, fileTask, e.getMessage());
      throw e;
    } catch (Exception ex) {
      handleTaskError(requestId, fileTask, "?????????????????????????????????????????????????????????");
      throw new BusinessIllegalStateException("??????????????????????????????????????????", ex);
    } finally {
      // finally??????
      finallyOp(fileTask);
      // ??????????????????????????????
      deleteFile(parent);
    }
  }

  protected void finallyOp(FileTask fileTask) {

  }

  protected boolean checkFileExist(FileTask fileTask) {
    return ossService.fileExit(fileTask.getPath());
  }

  protected void handleTaskError(String requestId, FileTask fileTask, String errorMsg) {
    // ????????????????????????
    stringRedisTemplate.delete(getTaskCacheKey(fileTask));
    String format = "{\"success\": false, \"msg\": \"%s\", \"complete\": true}";
    errorMsg = String.format(format, errorMsg);
    // ???????????????????????????3??????
    stringRedisTemplate
        .opsForValue()
        .set(
            RedisKeyMethods.getFileTaskMsgKey(fileTask.getTaskId(), requestId),
            errorMsg,
            60 * 10,
            TimeUnit.SECONDS);
    // ??????oss??????
    ossService.deleteFile(fileTask.getPath());
  }

  protected void handleTaskSuccess(String requestId, FileTask fileTask) {
    // ????????????????????????
    stringRedisTemplate.delete(getTaskCacheKey(fileTask));
    // language=JSON
    String msg = "{\"success\": true, \"msg\": \"success\", \"complete\": true}";
    // ?????????????????????10??????
    stringRedisTemplate
        .opsForValue()
        .set(
            RedisKeyMethods.getFileTaskMsgKey(fileTask.getTaskId(), requestId),
            msg,
            60 * 10,
            TimeUnit.SECONDS);
  }

  protected abstract void internalHandle(FileTask fileTask);

  protected abstract String getTaskCacheKey(FileTask fileTask);

  protected List<File> getLocalFiles(String localPath) {
    File file = Paths.get(localPath).toFile();
    File fileDir = new File(file.getParent(), FileUtil.mainName(file));
    File unzip = unZipFile(file, fileDir);
    return FileUtil.loopFiles(
        unzip,
        pathname -> !pathname.isDirectory() && (pathname.getParent() == null || !pathname.getParent()
            .contains("__MACOSX")));
  }

  protected File unZipFile(File file, File fileDir) {
    File unzip;
    try {
      unzip = ZipUtil.unzip(file, fileDir, StandardCharsets.UTF_8);
    } catch (Exception e) {
      unzip = ZipUtil.unzip(file, fileDir, Charset.forName("GBK"));
    }
    return unzip;
  }

  protected void deleteFile(File file) {
    try {
      FileUtil.del(file);
    } catch (IORuntimeException e) {
      log.error("[AbstractFileUploadTaskHandler.deleteFile] ??????????????????", e);
    }
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setDatasetInfoService(DatasetInfoService datasetInfoService) {
    this.datasetInfoService = datasetInfoService;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(
      RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }

  @Autowired
  public void setPublisher(RuleTaskAppendEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  protected interface SaveContent {

    void saveContent(File file, DatasetInfoPO datasetInfoPO, DataSetParseResult result);
  }
}
