package com.wl.xc.modelfun.entities.vo;


import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.SUCCESS;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.commons.enums.ResponseCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 标准的分页返回对象
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/4/13 18:15
 */
public class PageResultVo<T> extends ResultVo<T> {

  private static final long serialVersionUID = -9134662371402442024L;

  @Schema(description = "当前页码")
  @JsonProperty("current")
  private Long curPage;

  @Schema(description = "每页数量")
  @JsonProperty("pageSize")
  private Long pageSize;

  @Schema(description = "总记录数")
  @JsonProperty("total")
  private Long totalRows;

  @Schema(description = "总页数")
  @JsonProperty("pageCount")
  private Long pageCount;

  public PageResultVo() {
  }

  @JsonCreator(mode = Mode.PROPERTIES)
  public PageResultVo(@JsonProperty("msg") String msg, @JsonProperty("code") Integer code,
      @JsonProperty("success") Boolean success, @JsonProperty("data") T data, @JsonProperty("current") Long curPage,
      @JsonProperty("pageSize") Long pageSize, @JsonProperty("total") Long totalRows,
      @JsonProperty("pageCount") Long pageCount) {
    super(msg, code, success, data);
    this.curPage = curPage;
    this.pageSize = pageSize;
    this.totalRows = totalRows;
    this.pageCount = pageCount;
  }

  public PageResultVo(String msg, Integer code, Boolean success, T data) {
    super(msg, code, success, data);
    this.curPage = 1L;
    this.pageSize = 15L;
    this.totalRows = 0L;
    this.pageCount = 0L;
  }

  public static <T> PageResultVo<List<T>> create(ResponseCodeEnum codeEnum, String msg, Boolean success,
      PageVO<T> pageVO) {
    return new PageResultVo<>(msg, codeEnum.getCode(), success, pageVO.getRecords(), pageVO.getCurPage(),
        pageVO.getPageCount(), pageVO.getTotalRows(), pageVO.getPageCount());
  }

  public static <T> PageResultVo<List<T>> create(ResponseCodeEnum codeEnum, Boolean success,
      PageVO<T> pageVO) {
    return new PageResultVo<>(codeEnum.getMsg(), codeEnum.getCode(), success, pageVO.getRecords(), pageVO.getCurPage(),
        pageVO.getPageCount(), pageVO.getTotalRows(), pageVO.getPageCount());
  }

  public static <T> PageResultVo<List<T>> createSuccess(PageVO<T> pageVO) {
    return create(SUCCESS, SUCCESS.getMsg(), true, pageVO);
  }

  public static <T> PageResultVo<T> createSuccess(T data, Long curPage, Long pageSize,
      Long totalRows, Long pageCount) {
    return new PageResultVo<>(SUCCESS.getMsg(), SUCCESS.getCode(), true, data,
        curPage, pageSize, totalRows, pageCount);
  }

  public Long getCurPage() {
    return this.curPage;
  }

  public PageResultVo<T> setCurPage(Long curPage) {
    this.curPage = curPage;
    return this;
  }

  public Long getPageSize() {
    return this.pageSize;
  }

  public PageResultVo<T> setPageSize(Long pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  public Long getTotalRows() {
    return this.totalRows;
  }

  public PageResultVo<T> setTotalRows(Long totalRows) {
    this.totalRows = totalRows;
    return this;
  }

  public Long getPageCount() {
    return this.pageCount;
  }

  public PageResultVo<T> setPageCount(Long pageCount) {
    this.pageCount = pageCount;
    return this;
  }

  @Override
  public String toString() {
    return "PageResultVo{" +
        "curPage=" + curPage +
        ", pageSize=" + pageSize +
        ", totalRows=" + totalRows +
        ", pageCount=" + pageCount +
        ", msg=" + getMsg() +
        ", code=" + getCode() +
        ", success=" + getSuccess() +
        ", data=" + getData() +
        "} ";
  }
}
