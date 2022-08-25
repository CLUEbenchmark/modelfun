package com.wl.xc.modelfun.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.id.NanoId;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0
 * @date 2022/4/24 15:23
 */
public class PythonCheckUtil {

  private static final int os = getSystemType();

  private static final String tempPath =
      os == 1 ? "C:\\Users\\User\\AppData\\Local\\Temp\\" : "/tmp/tmp_download_file/";

  // 获取操作系统类型
  private static int getSystemType() {
    String os = System.getProperty("os.name");
    if (os.toLowerCase().startsWith("win")) {
      return 1;
    } else {
      return 0;
    }
  }

  public static String reWriteBody(String functionBody) {
    final String body = functionBody.replaceAll("\t", "    ");
    StringBuilder sb = new StringBuilder();
    String[] lines = body.split("\n");
    boolean isFirst = true;
    boolean firstLineCorrect = false;
    for (String line : lines) {
      if (line.trim().startsWith("#")) {
        continue;
      }
      if (isFirst) {
        isFirst = false;
        if (line.startsWith("    ")) {
          firstLineCorrect = true;
          sb.append(line).append("\n");
        } else {
          sb.append("    ").append(line).append("\n");
        }
      } else {
        if (firstLineCorrect) {
          sb.append(line).append("\n");
        } else {
          sb.append("    ").append(line).append("\n");
        }
      }
    }
    // 去除最后一个换行符
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  public static String checkPython(String functionBody) {
    String body = reWriteBody(functionBody);
    String pythonCode = "def test(x):\n" + body;
    try {
      return PythonCheckUtil.checkPythonCode(pythonCode);
    } catch (Exception e) {
      e.printStackTrace();
      return "服务器内部错误！";
    }
  }

  private static String checkPythonCode(String pythonCode) {
    // 把pythonCode写入一个python文件
    String nanoId = NanoId.randomNanoId();
    String fileName = tempPath + nanoId + ".py";
    File file = new File(fileName);
    try {
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        outputStream.write(pythonCode.getBytes());
      } catch (Exception e) {
        e.printStackTrace();
      }
      // 调用pylint进行代码检查
      String result = check(file);
      // 返回检查结果
      if (StringUtils.isNotBlank(result)) {
        // 返回有错误
        String[] split = result.split("\n");
        int start = 0;
        if (split.length > 1) {
          start = 1;
        }
        StringBuilder sb = new StringBuilder();
        String simpleName = file.getName();
        for (int i = start, splitLength = split.length; i < splitLength; i++) {
          String s = split[i];
          int index = s.indexOf(simpleName);
          s = s.substring(index + simpleName.length() + 1);
          sb.append(s).append("\n");
        }
        return sb.toString();
      } else {
        // 返回无错误
        return "";
      }
    } catch (BusinessIllegalStateException e) {
      throw new RuntimeException(e);
    } finally {
      FileUtil.del(file);
    }
  }

  private static String check(File file) throws BusinessIllegalStateException {
    String[] command = new String[]{"pylint", "-E", file.getAbsolutePath()};
    StringBuilder sb;
    try {
      Runtime runtime = Runtime.getRuntime();
      Process process = runtime.exec(command);
      InputStream inputStream = process.getInputStream();
      sb = new StringBuilder();
      byte[] bytes = new byte[1024];
      int len;
      while ((len = inputStream.read(bytes)) != -1) {
        sb.append(new String(bytes, 0, len));
      }
    } catch (Exception e) {
      throw new BusinessIllegalStateException(e);
    }
    String a = "     label = 88 if \"颜色\" in x else -1\n    # 返回标签\n    return label";
    return sb.toString();
  }
}
