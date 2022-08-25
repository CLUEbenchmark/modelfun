package com.wl.xc.modelfun.commons.methods;

import com.wl.xc.modelfun.commons.constants.FileCacheConstant;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 17:41
 */
public class RedisKeyMethods {

  public static final String FILE_ERROR_KEY = "fileError";

  /**
   * 根据任务ID生成数据集文件上传的key
   *
   * @param taskId 任务ID
   * @return 数据集文件上传的key，格式为 task:dataset:{taskId}
   */
  public static String generateDatasetKey(Long taskId) {
    return "task:dataset:" + taskId;
  }

  /**
   * 根据任务ID生成专家知识文件上传的key
   *
   * @param taskId 任务ID
   * @return 数据集文件上传的key，格式为 task:expert:{taskId}
   */
  public static String generateExpertKey(Long taskId) {
    return "task:expert:" + taskId;
  }

  public static String getFileTaskKey(Long taskId, String projectId) {
    return "fileTask:" + projectId + ":" + taskId;
  }

  public static String getFileTaskMsgKey(Long taskId, String requestId) {
    return "fileTask:" + taskId + ":" + requestId;
  }

  public static String getTaskTrainKey(long taskId, long trainRecordId) {
    return "modelTrain:" + taskId + ":" + trainRecordId;
  }

  public static String getTaskLabelKey(long taskId, long trainRecordId) {
    return "autoLabel:" + taskId + ":" + trainRecordId;
  }

  public static String getIntegrateFileCacheKey(long taskId) {
    return FileCacheConstant.INTEGRATE_FILE + ":" + taskId;
  }

  public static String getGPTCacheKey(long taskId, long ruleId) {
    return "GPT3" + ":" + taskId + ":" + ruleId;
  }

  public static String getIntegrateCacheKey(long taskId, long integrateId) {
    return "integrate" + ":" + taskId + ":" + integrateId;
  }

  public static String getOneClickCacheKey(long taskId) {
    return "oneClick" + ":" + taskId;
  }

  public static String getTextClickTaskKey(long taskId) {
    return "textClickTask" + ":" + taskId;
  }

  public static String getTextClickCacheKey(long taskId, long trainRecordId) {
    return "oneTextClick" + ":" + taskId + ":" + trainRecordId;
  }

  public static String getTextClickErrorKey(long taskId, long trainRecordId) {
    return "oneTextError" + ":" + taskId + ":" + trainRecordId;
  }

  public static String getTaskIntegrateKey(long taskId) {
    return "integrate" + ":" + taskId;
  }

  public static String getFewShowKey(long taskId, long recordId) {
    return "fewShot" + ":" + taskId + ":" + recordId;
  }
}
