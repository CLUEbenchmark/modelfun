package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.entities.req.MatrixDetailReq;
import com.wl.xc.modelfun.entities.req.ModelTrainReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelPageReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelReq;
import com.wl.xc.modelfun.entities.req.TrainReq;
import com.wl.xc.modelfun.entities.vo.MatrixVO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelResultVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TextTrainLabelDiffVO;
import com.wl.xc.modelfun.entities.vo.TrainResultVO;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.NerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/12 11:29
 */
@Tag(name = "模型训练管理")
@Slf4j
@RestController
@RequestMapping("/train")
public class ModelTrainController {

  private ModelTrainService modelTrainService;

  private NerService nerService;

  @Operation(method = "POST", summary = "根据任务ID分页查询训练记录")
  @PostMapping("/list")
  public PageResultVo<List<TrainResultVO>> getTrainRecordPage(@Validated @RequestBody ModelTrainReq req) {
    PageVO<TrainResultVO> pageVO = modelTrainService.getTrainRecordPage(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Operation(method = "POST", summary = "训练模型")
  @PostMapping("/model_train")
  public ResultVo<String> train(@Validated @RequestBody TrainReq req) {
    return modelTrainService.train(req);
  }

  @Operation(method = "GET", summary = "根据任务ID判断是否有训练任务正在运行")
  @GetMapping("/running/{taskId}")
  public ResultVo<Boolean> existRunningTrain(@PathVariable("taskId") Long taskId) {
    return modelTrainService.existRunningTrain(taskId);
  }

  @Operation(method = "POST", summary = "获取数据分析结果")
  @PostMapping("/train/label_detail")
  public ResultVo<List<NerTrainLabelResultVO>> getTrainLabelInfo(@Validated @RequestBody NerTrainLabelReq req) {
    return nerService.getTrainLabelInfo(req);
  }

  @Operation(method = "POST", summary = "获取标签对应的错误数据")
  @PostMapping("/train/label_diff")
  public PageResultVo<List<TextTrainLabelDiffVO>> getTrainLabelInfoDiff(
      @Validated @RequestBody NerTrainLabelPageReq req) {
    return modelTrainService.getTrainLabelInfoDiff(req);
  }

  @Operation(method = "POST", summary = "获取混淆矩阵")
  @PostMapping("/confusion_matrix")
  public ResultVo<MatrixVO> getConfusionMatrix(@Validated @RequestBody NerTrainLabelReq req) {
    return modelTrainService.getConfusionMatrix(req);
  }

  @Operation(method = "POST", summary = "下载分析结果")
  @PostMapping("/download/report")
  public ResultVo<String> downloadAnalysisResult(@Validated @RequestBody NerTrainLabelReq req) {
    return modelTrainService.downloadAnalysisResult(req);
  }

  @Operation(method = "POST", summary = "获取矩阵详情")
  @PostMapping("/matrix/detail")
  public PageResultVo<List<TextTrainLabelDiffVO>> getMatrixDetail(@Validated @RequestBody MatrixDetailReq req) {
    return modelTrainService.getMatrixDetail(req);
  }

  @Autowired
  public void setModelTrainService(ModelTrainService modelTrainService) {
    this.modelTrainService = modelTrainService;
  }

  @Autowired
  public void setNerService(NerService nerService) {
    this.nerService = nerService;
  }
}
