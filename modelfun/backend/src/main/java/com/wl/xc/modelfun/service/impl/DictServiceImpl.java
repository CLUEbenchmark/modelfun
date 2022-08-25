package com.wl.xc.modelfun.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wl.xc.modelfun.entities.po.SysDictPO;
import com.wl.xc.modelfun.entities.req.DictPageReq;
import com.wl.xc.modelfun.entities.req.DictReq;
import com.wl.xc.modelfun.entities.vo.DictDetailVO;
import com.wl.xc.modelfun.entities.vo.DictKeyValueVO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.service.DictService;
import com.wl.xc.modelfun.service.SysDictService;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字典服务实现类
 *
 * @version 1.0
 * @date 2022/4/12 15:37
 */
@Service
public class DictServiceImpl implements DictService {

  private SysDictService sysDictService;

  @Override
  public List<DictKeyValueVO> getDictList(DictReq req) {
    List<SysDictPO> result =
        sysDictService.getDictListByGroupAndKey(req.getMapGroup(), req.getMapKey());
    return result.stream()
        .map(dict -> new DictKeyValueVO(dict.getMapKey(), dict.getMapValue(), dict.getMapSort()))
        .collect(Collectors.toList());
  }

  @Override
  public PageVO<DictDetailVO> getDictDetailPage(DictPageReq req) {
    Page<SysDictPO> page = Page.of(req.getCurPage(), req.getPageSize());
    SysDictPO po = new SysDictPO();
    po.setMapGroup(req.getMapGroup());
    po.setMapKey(req.getMapKey());
    po.setMapValue(req.getMapValue());
    PageVO<SysDictPO> pageVO = sysDictService.getDictDetailPage(page, po);
    return pageVO.convert(v -> {
      DictDetailVO vo = new DictDetailVO();
      BeanCopyUtil.copy(v, vo);
      return vo;
    });
  }

  @Autowired
  public void setSysDictService(SysDictService sysDictService) {
    this.sysDictService = sysDictService;
  }
}
