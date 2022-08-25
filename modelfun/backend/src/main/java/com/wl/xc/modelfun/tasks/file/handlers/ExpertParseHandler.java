package com.wl.xc.modelfun.tasks.file.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.EXPERT_UPLOAD_TYPE;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import cn.hutool.core.io.FileUtil;
import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.ExpertParseModel;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.TaskExpertPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.TaskExpertService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/14 16:54
 */
@Slf4j
public class ExpertParseHandler extends AbstractFileUploadTaskHandler {

  private final Pattern pattern = Pattern.compile("([^\\t]+)\\t+([^\\t]+)");

  private TaskExpertService taskExpertService;

  private ExpertParseInternal internalHandle;

  @Override
  public FileTaskType getType() {
    return FileTaskType.EXPERT;
  }

  @Override
  protected void internalHandle(FileTask fileTask) {
    log.info("开始处理专家知识上传文件：{}", fileTask.getFileName());
    // 本地解压
    List<File> files = getLocalFiles(fileTask.getLocalPath());
    if (files == null || files.isEmpty()) {
      log.error("[ExpertParseHandler.handle] 获取压缩文件失败");
      handleTaskError(fileTask.getRequestId(), fileTask, "压缩文件夹为空文件夹！");
      return;
    }
    // 文件解析校验
    parseExpert(files);
    fileTask.getConfig().put("fileList", files);
    // 把事务范围尽量缩小，并且在不修改之前的代码的情况下，只能定义一个内部类，这样可以使用原有方法
    internalHandle.internalHandle(fileTask);
  }

  @Override
  public void afterHandle(FileTask fileTask) {
    Integer uploadType = (Integer) fileTask.getConfig().get(EXPERT_UPLOAD_TYPE);
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    if (uploadType == 1) {
      // 重新触发规则运行
      Map<String, Object> params = new HashMap<>();
      params.put(SESSION_UID, uid);
      publisher.publish(fileTask.getTaskId(), null, RuleTaskType.GLOBAL, params);
    }
    // 通过websocket通知客户端任务完成
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.EXPERT_PARSE_SUCCESS);
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(WebsocketDataDTO.create(infoPO.getId(), infoPO.getName(), "上传的专家知识解析成功", true));
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected void handleTaskError(String requestId, FileTask fileTask, String errorMsg) {
    super.handleTaskError(requestId, fileTask, errorMsg);
    // 通过websocket通知客户端任务失败，并告知失败原因
    String uid = (String) fileTask.getConfig().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.EXPERT_PARSE_FAIL);
    TaskInfoPO infoPO = taskInfoService.getById(fileTask.getTaskId());
    dto.setData(
        WebsocketDataDTO.create(
            infoPO.getId(),
            infoPO.getName(),
            String.format("上传的专家知识解析失败，失败原因为%s", errorMsg),
            false));
    WebSocketHandler.sendByUid(uid, dto);
  }

  private void parseExpert(List<File> files) {
    for (File file : files) {
      long count = 0;
      try (FileInputStream fis = new FileInputStream(file);
          BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
        String line;
        while ((line = reader.readLine()) != null) {
          count++;
          parseExpertLine(line);
        }
      } catch (BusinessArgumentException e) {
        throw new BusinessArgumentException("专家知识文件内容错误。文件名：" + file.getName() + "，行数：" + count);
      } catch (IOException e) {
        log.error("[ExpertParseHandler.parseExpert] 解析专家文件失败：{}", e.getMessage());
        throw new BusinessArgumentException(
            "专家知识文件：" + file.getName() + "解析失败！" + "第" + count + "行内容错误！");
      }
    }
  }

  private void parseExpertLine(String line) {
    while (line.endsWith("\t")) {
      line = line.substring(0, line.length() - 1);
    }
    Matcher matcher = pattern.matcher(line);
    if (!matcher.matches()) {
      throw new BusinessArgumentException("专家知识文件：" + line + "解析失败！");
    }
    // return new String[] {matcher.group(1), matcher.group(2)};
  }

  private void overrideUpload(FileTask fileTask, List<File> files) {
    // 删除原有数据
    taskExpertService.deleteAllByTaskId(fileTask.getTaskId());
    // 建立文件和任务的关系
    ExpertParseModel parseModel = getParseModel(fileTask, files);
    List<TaskExpertPO> expertPOList = parseModel.getExpertPOList();
    taskExpertService.saveBatch(expertPOList);

    final String ossParent = fileUploadProperties.getOssPrefix() + fileTask.getTaskId() + "/expert/";
    List<String> dirFiles = ossService.listDirFiles(ossParent);
    for (TaskExpertPO taskExpertPO : expertPOList) {
      dirFiles.remove(taskExpertPO.getFileAddress());
    }
    dirFiles.remove(ossParent);
    deleteAllExpertRuleAndRetrigger(fileTask);
    // 把解压后的文件上传到oss
    ossService.uploadFiles(parseModel.getUploadList());
    // 删除原有压缩文件
    ossService.deleteFile(fileTask.getPath());
    // 删除其他文件，追加上传，对于相同文件名的文件，直接覆盖，所以不需要删除，对于不同文件名的文件，需要删除
    ossService.deleteFiles(dirFiles);
  }


  private void appendUpload(FileTask fileTask, List<File> files) {
    checkAndRenameFile(fileTask, files);
    ExpertParseModel parseModel = getParseModel(fileTask, files);
    // 建立文件和任务的关系
    taskExpertService.saveBatch(parseModel.getExpertPOList());
    // 把解压后的文件上传到oss
    ossService.uploadFiles(parseModel.getUploadList());
    // 删除原有压缩文件
    ossService.deleteFile(fileTask.getPath());
  }


  private ExpertParseModel getParseModel(FileTask fileTask, List<File> files) {
    final String ossParent = fileUploadProperties.getOssPrefix() + fileTask.getTaskId() + "/expert/";
    List<FileUpload> uploadList =
        files.stream()
            .map(
                localFile -> {
                  FileUpload fileUpload = new FileUpload();
                  fileUpload.setFile(localFile);
                  fileUpload.setDestPath(ossParent + localFile.getName());
                  return fileUpload;
                })
            .collect(Collectors.toList());
    List<TaskExpertPO> expertPOList =
        uploadList.stream()
            .map(
                upload -> {
                  TaskExpertPO po = new TaskExpertPO();
                  po.setTaskId(fileTask.getTaskId());
                  po.setFileName(upload.getFile().getName());
                  po.setFileAddress(upload.getDestPath());
                  return po;
                })
            .collect(Collectors.toList());
    ExpertParseModel parseModel = new ExpertParseModel();
    parseModel.setExpertPOList(expertPOList);
    parseModel.setUploadList(uploadList);
    return parseModel;
  }

  private List<File> checkAndRenameFile(FileTask fileTask, List<File> files) {
    final String ossParent = fileUploadProperties.getOssPrefix() + fileTask.getTaskId() + "/expert/";
    // 获取目录下已经存在的文件
    List<String> dirFiles = ossService.listDirFiles(ossParent);
    // 目录下已经存在的文件名，变成set集合，方便查找
    Set<String> set = dirFiles.stream().map(FileUtil::getName).collect(Collectors.toSet());
    // 重命名本地文件名，避免和oss上的文件名重复
    for (int i = 0; i < files.size(); i++) {
      File file = files.get(i);
      String fileName = file.getName();
      if (set.contains(fileName)) {
        String newFileName = renameFile(fileName, set);
        File newFile = new File(file.getParent(), newFileName);
        file.renameTo(newFile);
        file = newFile;
        files.set(i, file);
      }
    }
    return files;
  }

  private String renameFile(String fileName, Set<String> existFiles) {
    String newFileName = fileName;
    int i = 1;
    while (existFiles.contains(newFileName)) {
      newFileName = String.format("%s-%d.%s", FileUtil.getName(fileName), i++, FileUtil.extName(fileName));
      i++;
    }
    return newFileName;
  }

  /**
   * 删除所有专家规则，删除专家规则对应的运行结果，并重新触发全局规则参数计算任务
   *
   * @param fileTask 任务
   */
  private void deleteAllExpertRuleAndRetrigger(FileTask fileTask) {
    RuleInfoPO infoPO = new RuleInfoPO();
    infoPO.setTaskId(fileTask.getTaskId());
    infoPO.setRuleType(RuleType.EXPERT.getType());
    List<RuleInfoPO> ruleList = ruleInfoService.selectBySelective(infoPO);
    // 删除专家知识规则的运行结果
    ruleList.forEach(
        po -> {
          // 测试集结果
          ruleResultService.deleteByTaskIdAndRuleId(po.getTaskId(), po.getId());
          // 未标注数据集结果
          ruleUnlabeledResultService.deleteByTaskIdAndRuleId(po.getTaskId(), po.getId());
        });
    // 删除现有的专家知识规则
    ruleInfoService.deleteByTaskIdAndRuleType(fileTask.getTaskId(), RuleType.EXPERT.getType());
  }

  @Override
  protected String getTaskCacheKey(FileTask fileTask) {
    return RedisKeyMethods.generateExpertKey(fileTask.getTaskId());
  }

  @Autowired
  public void setTaskExpertService(TaskExpertService taskExpertService) {
    this.taskExpertService = taskExpertService;
  }

  @Autowired
  public void setInternalHandle(ExpertParseInternal internalHandle) {
    this.internalHandle = internalHandle;
  }

  public class ExpertParseInternal implements InternalHandle {


    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void internalHandle(FileTask fileTask) {
      ArrayList<File> files = (ArrayList<File>) fileTask.getConfig().get("fileList");
      // 校验完成之后，根据文件上传类型，实现不同的逻辑。上传类型分为覆盖上传和追加上传
      // 覆盖上传，删除原有数据，重新插入新数据
      Integer uploadType = (Integer) fileTask.getConfig().get(EXPERT_UPLOAD_TYPE);
      if (uploadType == 1) {
        // 覆盖上传
        overrideUpload(fileTask, files);
      } else if (uploadType == 2) {
        // 追加上传
        appendUpload(fileTask, files);
      } else {
        throw new BusinessArgumentException("专家知识文件上传类型错误！");
      }
    }
  }

}
