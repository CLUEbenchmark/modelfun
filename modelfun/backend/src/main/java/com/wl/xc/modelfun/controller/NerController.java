package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.commons.enums.AutoLabelType;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelPageReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelReq;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.NerDataLabelDataVO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelDiffVO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelResultVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.NerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 这里又单独把ner的数据集上传，数据查询等分了一个接口出来。其实更合理的应该是对原有接口进行复用，根据任务类型做不同的业务处理。
 * <p>
 * 但是时间紧任务急，暂时先这样吧。
 *
 * @version 1.0
 * @date 2022/5/24 13:23
 */
@Tag(name = "ner任务相关接口")
@Slf4j
@RestController
@RequestMapping("/ner")
public class NerController {

  private NerService nerService;

  //===================================================数据集===================================================
  @Operation(method = "POST", summary = "分页查询ner测试集数据（可见部分）")
  @PostMapping("/testdata/list")
  public PageResultVo<List<NerDataLabelDataVO>> pageNerTestData(@Validated @RequestBody DatasetDetailReq req) {
    PageVO<NerDataLabelDataVO> pageVO = nerService.pageNerTestData(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Operation(method = "POST", summary = "分页查询ner训练集")
  @PostMapping("/traindata/list")
  public PageResultVo<List<NerDataLabelDataVO>> pageNerTrainData(@Validated @RequestBody DatasetDetailReq req) {
    PageVO<NerDataLabelDataVO> pageVO = nerService.pageNerTrainData(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Operation(method = "POST", summary = "修改数据集的标签")
  @PostMapping("/update/{taskId}/datalabel")
  public ResultVo<Boolean> updateNerDataLabel(@Validated @RequestBody NerDataLabelDataVO req,
      @PathVariable("taskId") Long taskId) {
    Integer dataType = req.getDataType();
    DatasetType type = DatasetType.getFromType(dataType);
    if (type != DatasetType.TEST && type != DatasetType.TRAIN) {
      return ResultVo.create("数据类型错误", -1, false, false);
    }
    return nerService.updateNerDataLabel(req, taskId);
  }

  //===================================================自动标注结果===================================================
  @Operation(method = "POST", summary = "分页查询ner自动标注结果")
  @PostMapping("/autolabel/list")
  public PageResultVo<List<NerDataLabelDataVO>> pageNerLabelResult(@Validated @RequestBody DatasetDetailReq req) {
    Integer dataType = req.getDataType();
    if (dataType == null) {
      return new PageResultVo<>("数据类型不能为空", -1, false, null);
    }
    if (dataType != AutoLabelType.CORRECT.getType() && dataType != AutoLabelType.DOUBTFUL.getType()) {
      return new PageResultVo<>("数据类型不能为空", -1, false, null);
    }
    PageVO<NerDataLabelDataVO> pageVO = nerService.pageNerLabelResult(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Operation(method = "POST", summary = "更新ner自动标注结果")
  @PostMapping("/update/{taskId}/auto_label")
  public ResultVo<Boolean> updateNerAutoLabelData(@Validated @RequestBody NerDataLabelDataVO req,
      @PathVariable("taskId") Long taskId) {
    Integer dataType = req.getDataType();
    if (dataType != AutoLabelType.CORRECT.getType() && dataType != AutoLabelType.DOUBTFUL.getType()) {
      return ResultVo.create("数据类型错误", -1, false, false);
    }
    return nerService.updateNerAutoLabelData(req, taskId);
  }

  @Operation(method = "POST", summary = "更新ner自动标注结果")
  @PostMapping("/del/{taskId}/auto_label")
  public ResultVo<Boolean> delNerAutoLabelData(@RequestBody NerDataLabelDataVO req,
      @PathVariable("taskId") Long taskId) {
    if (req.getId() == null) {
      return ResultVo.create("id不能为空", -1, false, false);
    }
    return nerService.delNerAutoLabelData(req, taskId);
  }

  @Operation(method = "POST", summary = "把自动标注的数据导入到训练集")
  @PostMapping("/import/{taskId}/auto_label")
  public ResultVo<Boolean> importAutoLabelData(@Validated @RequestBody NerDataLabelDataVO dataLabel,
      @PathVariable("taskId") Long taskId) {
    return nerService.importAutoLabelData(dataLabel, taskId);
  }

  @Operation(method = "POST", summary = "ner模型训练")
  @PostMapping("/train")
  public ResultVo<Boolean> nerTrain(@Validated @RequestBody TaskIdReq req) {
    return nerService.nerTrain(req.getTaskId());
  }

  @Operation(method = "POST", summary = "获取数据分析结果")
  @PostMapping("/train/label_detail")
  public ResultVo<List<NerTrainLabelResultVO>> getTrainLabelInfo(@Validated @RequestBody NerTrainLabelReq req) {
    return nerService.getTrainLabelInfo(req);
  }

  @Operation(method = "POST", summary = "获取标签对应的错误数据")
  @PostMapping("/train/label_diff")
  public PageResultVo<List<NerTrainLabelDiffVO>> getTrainLabelInfoDiff(
      @Validated @RequestBody NerTrainLabelPageReq req) {
    return nerService.getTrainLabelInfoDiff(req);
  }

  @Autowired
  public void setNerService(NerService nerService) {
    this.nerService = nerService;
  }
}
