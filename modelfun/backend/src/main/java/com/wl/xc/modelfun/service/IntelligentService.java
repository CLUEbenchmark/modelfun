package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.dto.OneClickCallbackDTO;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TextOneClickVO;

/**
 * @version 1.0
 * @date 2022/5/26 11:25
 */
public interface IntelligentService {

  /**
   * 一键标注，一键训练
   *
   * @param taskIdReq 任务id
   * @return ResultVo
   */
  ResultVo<Boolean> oneClickTrain(TaskIdReq taskIdReq);

  /**
   * 一键标注回调
   *
   * @param dto 一键标注回调dto
   * @return ResultVo
   */
  ResultVo<Boolean> oneClickCallback(OneClickCallbackDTO dto);

  /**
   * 判断是否存在文本一键标注任务，如果存在，还需返回任务当前阶段
   *
   * @param taskIdReq 任务id
   * @return ResultVo
   */
  ResultVo<TextOneClickVO> existTextOneClick(TaskIdReq taskIdReq);

  /**
   * 文本一键标注
   *
   * @param taskIdReq 任务id
   * @return ResultVo
   */
  ResultVo<Boolean> textOneClickTrain(TaskIdReq taskIdReq);
}
