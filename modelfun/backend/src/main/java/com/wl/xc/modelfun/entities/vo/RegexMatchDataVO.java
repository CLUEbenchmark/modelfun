package com.wl.xc.modelfun.entities.vo;

import java.util.List;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/7/5 14:40
 */
@Data
public class RegexMatchDataVO {

  /**
   * 覆盖率
   */
  private String coverage;
  /**
   * 匹配到的未标注数据集集合
   */
  private List<DatasetInfoVO> dataList;
}
