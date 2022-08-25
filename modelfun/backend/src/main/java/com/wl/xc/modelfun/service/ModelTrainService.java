package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.po.TrainResultPO;
import com.wl.xc.modelfun.entities.req.MatrixDetailReq;
import com.wl.xc.modelfun.entities.req.ModelTrainReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelPageReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelReq;
import com.wl.xc.modelfun.entities.req.TrainReq;
import com.wl.xc.modelfun.entities.vo.MatrixVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TextTrainLabelDiffVO;
import com.wl.xc.modelfun.entities.vo.TrainResultVO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/12 13:28
 */
public interface ModelTrainService {

  /**
   * 根据条件分页查询训练记录
   *
   * @param req 查询条件
   * @return PageVO<TrainRecordVO>
   */
  PageVO<TrainResultVO> getTrainRecordPage(ModelTrainReq req);

  /**
   * 训练模型
   *
   * @param req 训练请求
   * @return 训练任务ID
   */
  ResultVo<String> train(TrainReq req);

  void saveTrainResult(TrainResultPO trainResultPO);

  /**
   * 根据任务ID判断是否有训练任务正在运行
   *
   * @param taskId 任务ID
   * @return 是否有训练任务正在运行
   */
  ResultVo<Boolean> existRunningTrain(Long taskId);

  ResultVo<Boolean> saveTrainResultAsync(TrainCallbackDTO trainCallbackDTO);

  /**
   * 保存一条失败记录
   *
   * @param trainCallbackDTO 训练结果
   */
  void saveFailedTrainResult(TrainCallbackDTO trainCallbackDTO);

  PageResultVo<List<TextTrainLabelDiffVO>> getTrainLabelInfoDiff(NerTrainLabelPageReq req);

  /**
   * 下载分析结果
   *
   * @param req 请求参数
   * @return 文件下载地址
   */
  ResultVo<String> downloadAnalysisResult(NerTrainLabelReq req);

  ResultVo<MatrixVO> getConfusionMatrix(NerTrainLabelReq req);

  PageResultVo<List<TextTrainLabelDiffVO>> getMatrixDetail(MatrixDetailReq req);
}
