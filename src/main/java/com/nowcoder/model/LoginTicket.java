package com.nowcoder.model;

import java.util.Date;

// 视频 4-1：存储用户登陆注册后分发的 token（ticket）
// (token/session/ticket: 不同地方可能名字不同，但都是一串能标识用户身份的代码)；

// 之所以要单独建一张表来存 ticket（cookie）是因为：
// 同一个用户的多次请求可能打到不同的服务器上，所以需要在不同服务器间共享ticket，所以需要数据库单独建表；
// 类似于共享session（通过数据库（或者redis）同步）的概念；

// 拓展：单点登录的底层思想：
// 一次登录以后，就把token和对应信息放到某一个公共的地方（如session集群），之后所有的服务都可以访问；

// status初始为0，表示有效；改为1后表示 token失效；

public class LoginTicket {
  private int id;
  private int userId;
  private Date expired;
  private String ticket;
  private int status;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public Date getExpired() {
    return expired;
  }

  public void setExpired(Date expired) {
    this.expired = expired;
  }

  public String getTicket() {
    return ticket;
  }

  public void setTicket(String ticket) {
    this.ticket = ticket;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
