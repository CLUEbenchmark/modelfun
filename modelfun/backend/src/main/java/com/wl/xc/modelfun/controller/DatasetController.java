package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.validation.group.Update;
import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.req.DatasetReqDTO;
import com.wl.xc.modelfun.entities.req.LabelInfoReq;
import com.wl.xc.modelfun.entities.req.ParseProgressReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.DatasetSummaryVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TaskProgressVO;
import com.wl.xc.modelfun.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/11 17:29
 */
@Tag(name = "数据集管理")
@Slf4j
@RestController
@RequestMapping("/dataset")
@CrossOrigin()
public class DatasetController {

  private DatasetService datasetService;

  @Operation(method = "POST", summary = "根据数据类型分页查询数据集数据内容")
  @PostMapping("/detail")
  public PageResultVo<List<DatasetInfoVO>> getDatasetDetailPage(@Validated @RequestBody DatasetDetailReq req) {
    Integer dataType = req.getDataType();
    DatasetType type = DatasetType.getFromType(dataType);
    if (type == DatasetType.NULL) {
      return new PageResultVo<>("错误的数据类型", 8, false, null);
    }
    return datasetService.getDatasetDetailPage(req);
  }

  @Operation(method = "POST", summary = "根据任务ID和文件解析请求ID获取解析进度")
  @PostMapping("/parse_progress")
  public ResultVo<TaskProgressVO> getParseProgress(@Validated @RequestBody ParseProgressReq req) {
    return datasetService.getParseProgress(req);
  }

  @Operation(method = "POST", summary = "获取数据集概要信息")
  @PostMapping("/summary")
  public ResultVo<DatasetSummaryVO> getDatasetSummary(@Validated @RequestBody DatasetDetailReq req) {
    return datasetService.getDatasetSummary(req);
  }

  @Operation(method = "POST", summary = "更新标签集内容")
  @PostMapping("/update_label")
  public ResultVo<Boolean> updateLabelInfo(@RequestBody LabelInfoReq req) {
    return datasetService.updateLabelInfo(req);
  }

  @Operation(method = "POST", summary = "更新训练集标签")
  @PostMapping("/update_train")
  public ResultVo<Boolean> updateTrainData(@Validated({Update.class}) @RequestBody DatasetReqDTO req) {
    return datasetService.updateTrainData(req);
  }


  @Autowired
  public void setDatasetService(DatasetService datasetService) {
    this.datasetService = datasetService;
  }
}
