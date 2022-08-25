package com.wl.xc.modelfun.entities.model;

/**
 * @version 1.0
 * @date 2020.10.28 15:05
 */
public class LoginUser {

  private String username;
  private String password;

  public String getUsername() {
    return username;
  }

  public LoginUser setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public LoginUser setPassword(String password) {
    this.password = password;
    return this;
  }
}
