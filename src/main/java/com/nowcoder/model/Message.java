package com.nowcoder.model;

import java.util.Date;

// 视频 6： 业务开发 part 2：消息中心（站内信）功能：（包含 复杂SQL 和分页功能）

// 本功能更改的文件有
// com/nowcoder/model/Message.java
// com/nowcoder/dao/MessageDAO.java
// com/nowcoder/service/MessageService.java
// com/nowcoder/controller/MessageController.java

// 注意：getConversitionId函数需要进行手动改写；
// （conversationId不是传进来的，是在获取时自动合成的，由 fromId和 toId合成，合成时，小的id放前面，大的id放后面）；
// 无论是A发给B还是B发给A，conversationId都是一样的；

public class Message {
  private int id;
  private int fromId;
  private int toId;
  private String content;
  private Date createdDate;
  private int hasRead;
  private String conversationId;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getFromId() {
    return fromId;
  }

  public void setFromId(int fromId) {
    this.fromId = fromId;
  }

  public int getToId() {
    return toId;
  }

  public void setToId(int toId) {
    this.toId = toId;
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

  public int getHasRead() {
    return hasRead;
  }

  public void setHasRead(int hasRead) {
    this.hasRead = hasRead;
  }

  // 此函数需要手动改写（自动生成 conversationId）；
  public String getConversationId() {
    if (fromId < toId) {
      return String.format("%d_%d", fromId, toId);
    } else {
      return String.format("%d_%d", toId, fromId);
    }
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }
}
