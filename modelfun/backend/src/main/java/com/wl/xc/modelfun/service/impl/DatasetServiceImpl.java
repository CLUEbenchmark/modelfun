package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.NOT_EXIST;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.req.DatasetReqDTO;
import com.wl.xc.modelfun.entities.req.LabelInfoReq;
import com.wl.xc.modelfun.entities.req.ParseProgressReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.DatasetSummaryVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TaskProgressVO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.DatasetService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.NerTestDataService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/11 17:47
 */
@Service
public class DatasetServiceImpl implements DatasetService {

  private LabelInfoService labelInfoService;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private StringRedisTemplate stringRedisTemplate;

  private DatasetInfoService datasetInfoService;

  private TaskInfoService taskInfoService;

  private NerTestDataService nerTestDataService;

  private DatasetDetailService datasetDetailService;

  private ObjectMapper objectMapper;

  private final Map<DatasetType, Function<DatasetDetailReq, PageVO<DatasetInfoVO>>> PAGE_MAP = new HashMap<>();

  public DatasetServiceImpl() {
    PAGE_MAP.put(DatasetType.TEST_SHOW, this::getValData);
    PAGE_MAP.put(DatasetType.TRAIN, this::getTrainData);
    PAGE_MAP.put(DatasetType.UNLABELED, this::getUnlabelData);
    PAGE_MAP.put(DatasetType.LABEL, this::getLabelData);
  }

  @Override
  public PageResultVo<List<DatasetInfoVO>> getDatasetDetailPage(DatasetDetailReq req) {
    Integer dataType = req.getDataType();
    DatasetType type = DatasetType.getFromType(dataType);
    Function<DatasetDetailReq, PageVO<DatasetInfoVO>> pageFunction = PAGE_MAP.getOrDefault(type, this::unSupport);
    PageVO<DatasetInfoVO> pageVO = pageFunction.apply(req);
    return PageResultVo.createSuccess(pageVO);
  }

  @Override
  public ResultVo<TaskProgressVO> getParseProgress(ParseProgressReq req) {
    String value = stringRedisTemplate.opsForValue()
        .get(RedisKeyMethods.getFileTaskMsgKey(req.getTaskId(), req.getRequestId()));
    TaskProgressVO vo = new TaskProgressVO();
    if (StringUtils.isBlank(value)) {
      vo.setMsg("任务信息已过期！");
      return ResultVo.createSuccess(vo);
    }
    try {
      vo = objectMapper.readValue(value, TaskProgressVO.class);
    } catch (JsonProcessingException e) {
      vo.setMsg("任务信息已过期！");
      return ResultVo.createSuccess(vo);
    }
    return ResultVo.createSuccess(vo);
  }

  @Override
  public ResultVo<DatasetSummaryVO> getDatasetSummary(DatasetDetailReq req) {
    TaskInfoPO infoPO = taskInfoService.getById(req.getTaskId());
    DatasetSummaryVO vo = new DatasetSummaryVO();
    vo.setLabelCount(labelInfoService.countLabelInfoByTaskId(req.getTaskId()));
    vo.setUnlabelCount(unlabelDataService.countUnlabelDataByTaskId(req.getTaskId()));
    String s = stringRedisTemplate.opsForValue().get(RedisKeyMethods.generateDatasetKey(req.getTaskId()));
    if (StringUtils.isBlank(s)) {
      vo.setExitParseTask(false);
    } else {
      vo.setExitParseTask(true);
      vo.setRequestId(s);
    }
    if (infoPO.getTaskType() == 1) {
      vo.setTestDataCount(testDataService.countShowTestDataByTaskId(req.getTaskId()));
      vo.setTestDataTypeCount(testDataService.groupCountLabelByTaskId(req.getTaskId()));
      vo.setTrainDataCount(
          testDataService.countByTaskIdAndType(req.getTaskId(), DatasetType.TRAIN.getType()));
    } else {
      String cacheKey = RedisKeyMethods.getOneClickCacheKey(req.getTaskId());
      Boolean exit = stringRedisTemplate.hasKey(cacheKey);
      vo.setExitClickTask(Boolean.TRUE.equals(exit));
      vo.setTestDataCount(
          nerTestDataService.countByTaskIdAndType(req.getTaskId(), DatasetType.TEST.getType()));
      vo.setTrainDataCount(
          nerTestDataService.countByTaskIdAndType(req.getTaskId(), DatasetType.TRAIN.getType()));
    }
    DatasetInfoPO info = datasetInfoService.getLastDatasetInfo(req.getTaskId());
    vo.setUploadDateTime(info == null ? null : info.getUpdateDatetime());
    return ResultVo.createSuccess(vo);
  }

  @Override
  public ResultVo<Boolean> updateLabelInfo(LabelInfoReq req) {
    LabelInfoPO po = new LabelInfoPO();
    po.setId(req.getId());
    po.setDescription(req.getDescription());
    po.setExample(req.getExample());
    po.setUpdateDatetime(LocalDateTime.now());
    po.setUpdatePeople(ServletUserHolder.getUserByContext().getUserId().toString());
    labelInfoService.updateById(po);
    return ResultVo.createSuccess(true);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> updateTrainData(DatasetReqDTO req) {
    TestDataPO dataPO = testDataService.getById(req.getId());
    if (dataPO == null) {
      return ResultVo.create(NOT_EXIST, false, false);
    }
    LabelInfoPO labelInfo = labelInfoService.selectOneByTaskAndLabel(req.getTaskId(), req.getLabelId());
    if (labelInfo == null) {
      return ResultVo.create("标签不存在", 9, false, false);
    }
    TestDataPO po = new TestDataPO();
    po.setId(req.getId());
    po.setLabel(labelInfo.getLabelId());
    po.setLabelDes(labelInfo.getLabelDesc());
    testDataService.updateById(po);
    // 更新训练集时间
    DatasetDetailPO trainData = datasetDetailService.selectByTaskIdAndType(req.getTaskId(),
        DatasetType.TRAIN.getType());
    trainData.setUpdateDatetime(LocalDateTime.now());
    datasetDetailService.updateById(trainData);
    return ResultVo.createSuccess(true);
  }

  private PageVO<DatasetInfoVO> getLabelData(DatasetDetailReq req) {
    Page<LabelInfoPO> page = Page.of(req.getCurPage(), req.getPageSize());
    LabelInfoPO po = new LabelInfoPO();
    po.setTaskId(req.getTaskId());
    po.setLabelId(req.getLabelId());
    po.setLabelDesc(req.getSentence());
    po.setDescription(req.getDescription());
    PageVO<LabelInfoPO> pageVO = labelInfoService.pageLabelInfo(page, po);
    return pageVO.convert(l -> {
      DatasetInfoVO vo = new DatasetInfoVO();
      vo.setId(l.getId());
      vo.setLabel(l.getLabelId().toString());
      vo.setLabelDes(l.getLabelDesc());
      vo.setDescription(l.getDescription());
      vo.setExample(l.getExample());
      vo.setUpdateDatetime(l.getUpdateDatetime());
      return vo;
    });
  }

  /**
   * 获取验证集数据，即测试集中可见部分的数据，dataType为4
   *
   * @param req 参数
   * @return 验证集数据
   */
  private PageVO<DatasetInfoVO> getValData(DatasetDetailReq req) {
    return getDatasetInfoVOPageVO(req, DatasetType.TEST_SHOW);
  }

  /**
   * 获取训练集数据，dataType为8
   *
   * @param req 参数
   * @return 训练集数据
   */
  private PageVO<DatasetInfoVO> getTrainData(DatasetDetailReq req) {
    return getDatasetInfoVOPageVO(req, DatasetType.TRAIN);
  }

  private PageVO<DatasetInfoVO> getDatasetInfoVOPageVO(DatasetDetailReq req, DatasetType testShow) {
    Page<TestDataPO> page = Page.of(req.getCurPage(), req.getPageSize());
    page.orders().add(OrderItem.desc(TestDataPO.COL_DATA_ID));
    TestDataPO po = new TestDataPO();
    po.setTaskId(req.getTaskId());
    po.setSentence(req.getSentence());
    po.setLabel(req.getLabelId());
    po.setDataType(testShow.getType());
    PageVO<TestDataPO> pageVO = testDataService.pageDatasetDetail(page, po);
    return pageVO.convert(l -> {
      DatasetInfoVO vo = new DatasetInfoVO();
      vo.setId(l.getId());
      vo.setDataId(l.getDataId());
      vo.setSentence(l.getSentence());
      if (l.getLabel() != null) {
        vo.setLabel(l.getLabel().toString());
        vo.setLabelDes(l.getLabelDes());
      }
      vo.setDataType(l.getDataType());
      return vo;
    });
  }

  private PageVO<DatasetInfoVO> getUnlabelData(DatasetDetailReq req) {
    Page<UnlabelDataPO> page = Page.of(req.getCurPage(), req.getPageSize());
    UnlabelDataPO po = new UnlabelDataPO();
    po.setTaskId(req.getTaskId());
    po.setSentence(req.getSentence());
    PageVO<UnlabelDataPO> pageVO = unlabelDataService.pageDatasetDetail(page, po);
    return pageVO.convert(
        l -> {
          DatasetInfoVO vo = new DatasetInfoVO();
          vo.setDataId(l.getDataId());
          vo.setSentence(l.getSentence());
          vo.setDataType(DatasetType.UNLABELED.getType());
          vo.setId(l.getId());
          return vo;
        });
  }

  private PageVO<DatasetInfoVO> unSupport(DatasetDetailReq req) {
    throw new BusinessArgumentException("错误的数据集类型");
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setDatasetInfoService(DatasetInfoService datasetInfoService) {
    this.datasetInfoService = datasetInfoService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setNerTestDataService(NerTestDataService nerTestDataService) {
    this.nerTestDataService = nerTestDataService;
  }
}
