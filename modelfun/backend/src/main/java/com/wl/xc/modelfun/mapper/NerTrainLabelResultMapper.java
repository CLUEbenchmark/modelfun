package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.NerTrainLabelResultPO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/6/9 18:08
 */
public interface NerTrainLabelResultMapper extends BaseMapper<NerTrainLabelResultPO> {

  List<NerTrainLabelResultPO> getTemplateByTrainRecordId(Long trainRecordId);
}
