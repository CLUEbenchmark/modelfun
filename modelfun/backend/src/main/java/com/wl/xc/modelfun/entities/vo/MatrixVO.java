package com.wl.xc.modelfun.entities.vo;

import java.util.List;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/7/6 16:05
 */
@Data
public class MatrixVO {

  private List<List<Integer>> matrix;

  private List<String> labels;

}
