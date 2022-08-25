package com.wl.xc.modelfun.entities.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页数据对象
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/4/14 11:24
 */
public class PageVO<T> {

  private static final boolean IS_SINGLE;
  private static final int MAX_LENGTH = 1000;

  static {
    int processors = Runtime.getRuntime().availableProcessors();
    IS_SINGLE = processors <= 1;
  }

  private Long curPage;
  private Long pageSize;
  private Long totalRows;
  private Long pageCount;
  private List<T> records;

  public PageVO() {
    this.curPage = 1L;
    this.pageSize = 0L;
    this.totalRows = 0L;
    this.pageCount = 0L;
    this.records = null;
  }

  public PageVO(Long curPage, Long pageSize, Long totalRows, Long pageCount, List<T> records) {
    this.curPage = curPage;
    this.pageSize = pageSize;
    this.totalRows = totalRows;
    this.pageCount = pageCount;
    this.records = records;
  }

  /**
   * 转换。该转换不会引发cast异常
   *
   * @param mapper 转换函数
   * @param <R>    转换后的类型
   * @return 转换后的page对象
   */
  @SuppressWarnings("unchecked")
  public <R> PageVO<R> convert(Function<? super T, ? extends R> mapper) {
    if (records == null || records.isEmpty()) {
      return (PageVO<R>) this;
    }
    ArrayList<R> rs = new ArrayList<>();
    for (T data : records) {
      R result = mapper.apply(data);
      rs.add(result);
    }
    return ((PageVO<R>) this).setRecords(rs);
  }

  @SuppressWarnings("unchecked")
  public <R> PageVO<R> parallelConvert(Function<? super T, ? extends R> mapper) {
    if (records == null || records.isEmpty()) {
      return (PageVO<R>) this;
    }
    List<R> list;
    if (records.size() > MAX_LENGTH && !IS_SINGLE) {
      list = records.parallelStream().map(mapper).collect(Collectors.toList());
    } else {
      // 如果是单核处理器或者列表数据量小于MAX_LENGTH，使用普通的循环，效率大于stream
      ArrayList<R> rs = new ArrayList<>();
      for (T data : records) {
        R result = mapper.apply(data);
        rs.add(result);
      }
      list = rs;
    }
    return ((PageVO<R>) this).setRecords(list);
  }

  public Long getCurPage() {
    return this.curPage;
  }

  public PageVO<T> setCurPage(Long curPage) {
    this.curPage = curPage;
    return this;
  }

  public Long getPageSize() {
    return this.pageSize;
  }

  public PageVO<T> setPageSize(Long pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public Long getTotalRows() {
    return this.totalRows;
  }

  public PageVO<T> setTotalRows(Long totalRows) {
    this.totalRows = totalRows;
    return this;
  }

  public Long getPageCount() {
    return this.pageCount;
  }

  public PageVO<T> setPageCount(Long pageCount) {
    this.pageCount = pageCount;
    return this;
  }

  public List<T> getRecords() {
    return this.records;
  }

  public PageVO<T> setRecords(List<T> records) {
    this.records = records;
    return this;
  }

  @Override
  public String toString() {
    return "PageVO{" +
        "curPage=" + curPage +
        ", pageSize=" + pageSize +
        ", totalRows=" + totalRows +
        ", pageCount=" + pageCount +
        ", records=" + records +
        '}';
  }
}
