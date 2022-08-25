package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.SysDictPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.SysDictMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class SysDictService extends ServiceImpl<SysDictMapper, SysDictPO> {

  public List<SysDictPO> getDictListByGroupAndKey(String group, String key) {
    return baseMapper.selectList(Wrappers.<SysDictPO>query()
        .eq(StringUtils.isNotBlank(group), SysDictPO.COL_MAP_GROUP, group)
        .eq(StringUtils.isNotBlank(key), SysDictPO.COL_MAP_KEY, key));
  }

  public PageVO<SysDictPO> getDictDetailPage(IPage<SysDictPO> page, SysDictPO entity) {
    IPage<SysDictPO> pageResult = baseMapper.selectPage(page, Wrappers.<SysDictPO>query()
        .eq(StringUtils.isNotBlank(entity.getMapKey()), SysDictPO.COL_MAP_KEY, entity.getMapKey())
        .eq(StringUtils.isNotBlank(entity.getMapValue()), SysDictPO.COL_MAP_VALUE, entity.getMapValue())
        .eq(StringUtils.isNotBlank(entity.getMapGroup()), SysDictPO.COL_MAP_GROUP, entity.getMapGroup()));
    return PageUtil.convert(pageResult);
  }
}



