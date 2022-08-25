package com.wl.xc.modelfun.entities.model;

import java.io.File;
import java.util.Map;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/14 17:50
 */
@Data
public class FileUpload {

  private File file;

  private String destPath;

  private String contentType;

  private Map<String, String> tagMap;

}
