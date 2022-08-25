package com.wl.xc.modelfun.tasks.file.handlers;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/6/7 17:41
 */
@Component
public class FileService {

  private FileUploadProperties fileUploadProperties;

  private OssService ossService;

  private DatasetDetailService datasetDetailService;

  @Transactional(rollbackFor = Exception.class)
  public void uploadDatasetFiles(
      FileTask fileTask,
      List<File> files,
      Integer datasetId,
      String showFileName,
      String unShowFileName) {
    final String ossParent =
        fileUploadProperties.getOssPrefix() + fileTask.getTaskId() + "/dataset/";
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
    // 入库文件的地址
    List<DatasetDetailPO> collect =
        uploadList.stream()
            .map(
                i -> {
                  DatasetDetailPO detailPO = new DatasetDetailPO();
                  detailPO.setDataSetId(datasetId);
                  detailPO.setFileAddress(i.getDestPath());
                  detailPO.setTaskId(fileTask.getTaskId());
                  if (showFileName != null && i.getFile().getName().contains(showFileName)) {
                    detailPO.setFileType(DatasetType.TEST_SHOW.getType());
                  } else if (unShowFileName != null
                      && i.getFile().getName().contains(unShowFileName)) {
                    detailPO.setFileType(DatasetType.TEST_UN_SHOW.getType());
                  } else {
                    DatasetType datasetType = DatasetType.getFromName(i.getFile().getName());
                    detailPO.setFileType(datasetType.getType());
                  }
                  return detailPO;
                })
            .collect(Collectors.toList());
    collect.forEach(i -> datasetDetailService.insertOrUpdate(i));
    // 把文件上传到oss
    ossService.uploadFiles(uploadList);
    // 删除除了这次上传的其他文件
    List<String> dirFiles = ossService.listDirFiles(ossParent);
    collect.forEach(i -> dirFiles.remove(i.getFileAddress()));
    // 原始zip不删除
    dirFiles.remove(fileTask.getPath());
    dirFiles.remove(ossParent);
    ossService.deleteFiles(dirFiles);
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }
}
