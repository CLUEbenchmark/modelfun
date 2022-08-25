package com.wl.xc.modelfun.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @version 1.0
 * @date 2022.4.16 1:14
 */
public class CalcUtil {

  /**
   * 高精度的除法运算，默认保留两位小数
   *
   * @param a 除数
   * @param b 被除数
   * @return 结果
   */
  public static String divide(int a, int b, int scale) {
    return BigDecimal.valueOf(a)
        .divide(BigDecimal.valueOf(b), scale, RoundingMode.HALF_UP)
        .toString();
  }

  /**
   * 高精度的除法运算，默认保留两位小数
   *
   * @param a     除数
   * @param b     被除数
   * @param scale 保留的小数位数
   * @return 计算结果的字符串
   */
  public static String divide(long a, long b, int scale) {
    return BigDecimal.valueOf(a)
        .divide(BigDecimal.valueOf(b), scale, RoundingMode.HALF_UP)
        .toString();
  }

  /**
   * 高精度的乘法运算，默认保留两位小数
   *
   * @param a 被乘数
   * @param b 乘数
   * @return 结果
   */
  public static String multiply(int a, int b) {
    return BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b)).toString();
  }

  public static String multiply(String a, String b) {
    return new BigDecimal(a)
        .multiply(new BigDecimal(b))
        .toString();
  }

  public static String multiply(String a, String b, int scale) {
    return new BigDecimal(a)
        .multiply(new BigDecimal(b))
        .setScale(scale, RoundingMode.HALF_UP)
        .toString();
  }

  public static String multiply(String a, String b, int scale, boolean up) {
    return new BigDecimal(a)
        .multiply(new BigDecimal(b))
        .setScale(scale, up ? RoundingMode.HALF_UP : RoundingMode.DOWN)
        .toString();
  }
}
