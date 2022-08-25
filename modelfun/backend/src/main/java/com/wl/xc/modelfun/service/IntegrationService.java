package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.dto.FewShotCallbackDTO;
import com.wl.xc.modelfun.entities.dto.IntegrateCallbackDTO;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.req.AutoLabelResultReq;
import com.wl.xc.modelfun.entities.req.IntegrationReq;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.IntegrateOverviewVO;
import com.wl.xc.modelfun.entities.vo.IntegrationResultVO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;

/**
 * @version 1.0
 * @date 2022/4/12 10:55
 */
public interface IntegrationService {

  /**
   * 根据任务id分页查询集成情况
   *
   * @param req 分页参数
   * @return 分页结果
   */
  PageVO<IntegrationResultVO> getIntegrationPage(IntegrationReq req);

  /**
   * 对任务进行数据集成
   *
   * @param req 任务id
   * @return 集成结果
   */
  ResultVo<Long> integrate(TaskIdReq req);

  /**
   * 规则集成前的检查
   *
   * @param taskId 任务id
   * @return 检查结果，如果失败则返回失败的原因
   */
  ResultVo<Long> checkBeforeIntegrate(Long taskId, DatasetInfoPO datasetInfo);

  void saveNewIntegration(IntegrationRecordsPO po);

  /**
   * 根据任务ID判断是否有集成任务正在运行
   *
   * @param taskId 任务ID
   * @return 是否有集成任务正在运行
   */
  ResultVo<Boolean> existRunningIntegration(Long taskId);

  /**
   * 根据任务ID查询集成返回的标签结果
   *
   * @param req 任务ID
   * @return 标签结果
   */
  PageVO<DatasetInfoVO> getIntegrationLabelPage(IntegrationReq req);

  /**
   * 标注结果总览
   *
   * @param req 任务ID
   * @return 标注结果总览
   */
  ResultVo<IntegrateOverviewVO> getIntegrateOverview(TaskIdReq req);

  /**
   * 发起自动标注任务
   *
   * @param req 任务ID
   * @return 标注结果
   */
  ResultVo<Long> autoLabel(TaskIdReq req);

  /**
   * 根据任务ID判断是否有自动标注任务正在运行
   *
   * @param taskId 任务ID
   * @return 是否有自动标注任务正在运行
   */
  ResultVo<Boolean> existLabelingTask(Long taskId);

  /**
   * 集成接口回调
   *
   * @param integrateCallbackDTO 回调参数
   * @return 是否成功
   */
  ResultVo<Boolean> saveIntegrationAsync(IntegrateCallbackDTO integrateCallbackDTO);

  /**
   * 根据记录ID批量删除自动标注结果
   *
   * @param req 请求
   * @return 是否成功
   */
  ResultVo<Boolean> delAutoLabelResult(AutoLabelResultReq req);

  /**
   * 修改自动标注结果的标签
   *
   * @param req 请求
   * @return 返回结果
   */
  ResultVo<Boolean> editAutoLabelResult(AutoLabelResultReq req);

  /**
   * ner任务的自动标注
   *
   * @param req 任务ID
   * @return 标注结果
   */
  ResultVo<Long> autoLabelNer(TaskIdReq req);

  ResultVo<Boolean> saveFewShotAsync(FewShotCallbackDTO callbackDTO);
}
