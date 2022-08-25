package com.wl.xc.modelfun.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.vo.PageVO;

/**
 * @version 1.0
 * @date 2022/4/11 18:11
 */
public class PageUtil {


  public static <T> PageVO<T> convert(IPage<T> page) {
    PageVO<T> pageVO = new PageVO<>();
    pageVO.setCurPage(page.getCurrent());
    pageVO.setPageCount(page.getPages());
    pageVO.setPageSize(page.getSize());
    pageVO.setTotalRows(page.getTotal());
    pageVO.setRecords(page.getRecords());
    return pageVO;
  }

  /**
   * 根据总数计算总页数
   *
   * @param totalCount 总数
   * @param pageSize   每页数
   * @return 总页数
   */
  public static long totalPage(long totalCount, long pageSize) {
    if (pageSize == 0) {
      return 0;
    }
    return totalCount % pageSize == 0 ? (totalCount / pageSize) : (totalCount / pageSize + 1);
  }

}
