package com.nowcoder.model;

import java.util.Date;

// 视频 6：评论中心功能代码：

public class Comment {
  private int id;
  private int userId;

  // 评论可以针对两种实体：
  // 评论 既可以针对一个问题（作为一条答案） 也可以针对另一条评论（作为一个评论的回复）；
  private int entityType;
  private int entityId;

  private String content;
  private Date createdDate;

  // status 用来标记记录是否已被删除，1表示这条记录已被删除，默认正常状态为0；
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

  public int getEntityId() {
    return entityId;
  }

  public void setEntityId(int entityId) {
    this.entityId = entityId;
  }

  public int getEntityType() {
    return entityType;
  }

  public void setEntityType(int entityType) {
    this.entityType = entityType;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
