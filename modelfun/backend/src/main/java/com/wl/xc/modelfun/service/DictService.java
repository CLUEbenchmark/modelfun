package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.req.DictPageReq;
import com.wl.xc.modelfun.entities.req.DictReq;
import com.wl.xc.modelfun.entities.vo.DictDetailVO;
import com.wl.xc.modelfun.entities.vo.DictKeyValueVO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/12 15:36
 */
public interface DictService {

  /**
   * 查询字典
   *
   * @param req 字典请求
   * @return 字典列表
   */
  List<DictKeyValueVO> getDictList(DictReq req);

  PageVO<DictDetailVO> getDictDetailPage(DictPageReq req);
}
