package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.dto.NerAutoLabelCallbackDTO;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO;
import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelPageReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelReq;
import com.wl.xc.modelfun.entities.vo.NerDataLabelDataVO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelDiffVO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelResultVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/5/25 14:00
 */
public interface NerService {

  PageVO<NerDataLabelDataVO> pageNerTestData(DatasetDetailReq req);

  PageVO<NerDataLabelDataVO> pageNerLabelResult(DatasetDetailReq req);

  /**
   * 分页查询训练集内容
   *
   * @param req 查询条件
   * @return 分页结果
   */
  PageVO<NerDataLabelDataVO> pageNerTrainData(DatasetDetailReq req);

  /**
   * 更新数据对应的标签
   *
   * @param req    标签内容
   * @param taskId 任务id
   * @return 更新结果
   */
  ResultVo<Boolean> updateNerDataLabel(NerDataLabelDataVO req, Long taskId);

  /**
   * 更新自动标注结果对应的标签
   *
   * @param req    标签内容
   * @param taskId 任务id
   * @return 更新结果
   */
  ResultVo<Boolean> updateNerAutoLabelData(NerDataLabelDataVO req, Long taskId);

  /**
   * 把自动标注结果中的错误数据导入到训练集
   *
   * @param dataLabel 自动标注结果
   * @param taskId    任务id
   * @return 更新结果
   */
  ResultVo<Boolean> importAutoLabelData(NerDataLabelDataVO dataLabel, Long taskId);

  ResultVo<Boolean> saveTrainAsync(NerTrainCallbackDTO dto);

  ResultVo<Boolean> saveAutoAsync(NerAutoLabelCallbackDTO dto);

  ResultVo<Boolean> nerTrain(Long taskId);

  ResultVo<List<NerTrainLabelResultVO>> getTrainLabelInfo(NerTrainLabelReq req);

  PageResultVo<List<NerTrainLabelDiffVO>> getTrainLabelInfoDiff(NerTrainLabelPageReq req);

  ResultVo<Boolean> delNerAutoLabelData(NerDataLabelDataVO req, Long taskId);
}
