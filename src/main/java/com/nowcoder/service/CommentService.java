package com.nowcoder.service;

// 视频 6：评论中心功能代码：

// service调用了 DAO中的方法，
// service中的方法大部分和 DAO中的方法一一对应；

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {
  @Autowired CommentDAO commentDAO;

  @Autowired SensitiveService sensitiveService;

  // 增加评论：
  public int addComment(Comment comment) {
    // 在增加评论前，需要对 HTML标签 和 敏感词进行过滤（使用了上一视频中写的 SensitiveService）;
    comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
    comment.setContent(sensitiveService.filter(comment.getContent()));

    return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
  }

  // 查询评论：
  public List<Comment> selectCommentsByEntity(int entityId, int entityType) {
    return commentDAO.selectCommentsByEntity(entityId, entityType);
  }

  // 查询评论数量：
  public int getCommentCount(int entityId, int entityType) {
    return commentDAO.getCommentCount(entityId, entityType);
  }

  // 删除评论：
  public boolean deleteComment(int commentId) {
    return commentDAO.updateStatus(commentId, 1) > 0;
  }
}
