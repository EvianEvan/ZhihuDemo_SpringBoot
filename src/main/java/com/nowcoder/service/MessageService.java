package com.nowcoder.service;

import com.nowcoder.dao.MessageDAO;
import com.nowcoder.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// 视频 6： 业务开发 part 2：消息中心（站内信）功能：（包含 复杂SQL 和分页功能）

// 本功能更改的文件有
// com/nowcoder/model/Message.java
// com/nowcoder/dao/MessageDAO.java
// com/nowcoder/service/MessageService.java
// com/nowcoder/controller/MessageController.java

@Service
public class MessageService {
  @Autowired MessageDAO messageDAO;

  // 对站内信也应该进行敏感词过滤；
  @Autowired SensitiveService sensitiveService;

  // 增：添加一条私信；
  public int addMessage(Message message) {
    message.setContent(sensitiveService.filter(message.getContent()));
    // 添加成功则返回 id，失败则返回 0；
    return messageDAO.addMessage(message) > 0 ? message.getId() : 0;
  }

  // 查 1：查询 私信消息列表 的详情页:
  public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
    return messageDAO.getConversationDetail(conversationId, offset, limit);
  }

  // 查 2：查询 私信消息列表 的列表页：
  public List<Message> getConversationList(int userId, int offset, int limit) {
    return messageDAO.getConversationList(userId, offset, limit);
  }

  // 查3：查询未读消息的数量：
  public int getConversationUnreadCount(int userId, String conversationId) {
    return messageDAO.getConversationUnreadCount(userId, conversationId);
  }

  // 改：我自己写的：将已读消息的状态置为已读：阅读站内信以后清除未读数字：
  public boolean updateUnread(int userId, String conversationId) {
    return messageDAO.updateUnread(userId, conversationId) > 0;
  }
}
