package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.IntegrationResultPO;
import com.wl.xc.modelfun.entities.po.IntegrationWithRule;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface IntegrationResultMapper extends BaseMapper<IntegrationResultPO> {

  List<IntegrationWithRule> pageResultByTaskId(IPage<IntegrationWithRule> page, @Param("taskId") Long taskId);
}