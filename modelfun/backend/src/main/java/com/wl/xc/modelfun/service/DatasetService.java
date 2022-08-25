package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.req.DatasetReqDTO;
import com.wl.xc.modelfun.entities.req.LabelInfoReq;
import com.wl.xc.modelfun.entities.req.ParseProgressReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.DatasetSummaryVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TaskProgressVO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/11 17:47
 */
public interface DatasetService {

  /**
   * 分页查询数据内容
   *
   * @param req 查询条件
   * @return 分页结果
   */
  PageResultVo<List<DatasetInfoVO>> getDatasetDetailPage(DatasetDetailReq req);

  /**
   * 根据任务ID查询文件解析进度
   *
   * @param req 解析任务ID
   * @return 进度
   */
  ResultVo<TaskProgressVO> getParseProgress(ParseProgressReq req);

  ResultVo<DatasetSummaryVO> getDatasetSummary(DatasetDetailReq req);

  /**
   * 更新标签内容
   *
   * @param req 标签内容
   * @return 更新结果
   */
  ResultVo<Boolean> updateLabelInfo(LabelInfoReq req);

  /**
   * 更新训练集标签
   *
   * @param req 标签内容
   * @return 更新结果
   */
  ResultVo<Boolean> updateTrainData(DatasetReqDTO req);
}
