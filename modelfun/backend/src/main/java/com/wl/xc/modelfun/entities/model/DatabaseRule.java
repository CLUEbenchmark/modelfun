package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用于描述数据库规则的实体类
 *
 * @version 1.0
 * @date 2022/4/15 13:40
 */
@Data
public class DatabaseRule {


  /**
   * 服务器地址和端口号
   */
  @JsonProperty("host")
  private String host;
  /**
   * 端口号
   */
  @JsonProperty("port")
  private Integer port;
  /**
   * 用户名
   */
  @JsonProperty("user")
  private String user;
  /**
   * 密码
   */
  @JsonProperty("password")
  private String password;
  /**
   * 数据库类型，1：MySQL目前只支持mysql
   */
  @JsonProperty("databaseType")
  private Integer databaseType;
  /**
   * 数据库名
   */
  @JsonProperty("database")
  private String database;
  /**
   * 表名
   */
  @JsonProperty("table")
  private String table;
  /**
   * 语料字段名
   */
  @JsonProperty("sentenceColumn")
  private String sentenceColumn;
  /**
   * 标签字段名
   */
  @JsonProperty("labelColumn")
  private String labelColumn;
}
