package com.wl.xc.modelfun.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.10.28 13:42
 */
public final class RequestUtil {


  private RequestUtil() {
  }

  /**
   * 从request中获取请求体
   *
   * @param request     HttpServletRequest
   * @param charSetName 编码格式
   * @return String Body
   */
  public static String getRequestBody(HttpServletRequest request, String charSetName) {

    try (ServletInputStream inputStream = request.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charSetName))) {
      return reader.lines().collect(Collectors.joining("\n"));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 从request中获取请求体
   *
   * @param request HttpServletRequest
   * @return String Body
   */
  public static String getRequestBody(HttpServletRequest request) {
    return getRequestBody(request, "UTF-8");
  }

}
