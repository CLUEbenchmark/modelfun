package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.GptCachePO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/5/16 11:33
 */
public interface GptCacheMapper extends BaseMapper<GptCachePO> {

  List<Long> selectCachedId(
      @Param("taskId") Long taskId, @Param("dataType") Integer dataType);
}