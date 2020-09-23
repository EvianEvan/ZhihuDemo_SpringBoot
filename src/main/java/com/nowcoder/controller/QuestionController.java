package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ZhihuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// 视频 5 代码： 问题提交按钮功能 和 问题详情页：
// Ajax请求: 请求不提交到一个页面，而是提交到后台，后台会返回一个json串，前台再自己做一些动态的页面刷新；

// 视频 6 添加代码：在问题详情页中添加答案（针对问题的评论）的显示功能；

// 返回值是一个json字符串
// (json类似于：{'code':'0','msg':'error'}，即通过逗号和冒号分开的一种字符串格式)：
@Controller
public class QuestionController {
  private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

  @Autowired QuestionService questionService;

  @Autowired UserService userService;

  // 获取当前登录用户；
  @Autowired HostHolder hostHolder;

  // 向后台写数据时，都使用 POST方法；
  // 通过添加一个返回值code来表示返回值是否正确（一般使 code为 0时表示正确）；
  //  @ResponseBody：表示返回一个字符串（JSON）；
  @RequestMapping(
      value = "/question/add",
      method = {RequestMethod.POST})
  @ResponseBody
  public String addQuestion(
      @RequestParam("title") String title, @RequestParam("content") String content) {
    try {
      // 构造 Question：
      Question question = new Question();
      question.setTitle(title);
      question.setContent(content);
      question.setCreatedDate(new Date());
      // 保存当前登录的用户 ID:( 若当前未登录,则保存一个匿名的 ID）
      if (hostHolder.getUser() == null) {

        // question.setUserId(ZhihuUtil.ANONYMOUS_USERID);

        // 也可以不设置匿名用户，直接返回 999 表示未登录；
        return ZhihuUtil.getJSONString(999);

      } else {
        question.setUserId(hostHolder.getUser().getId());
      }
      // 保存 Question：若成功则返回0，失败返回1：
      if (questionService.addQuestion(question) > 0) {
        return ZhihuUtil.getJSONString(0);
      }

    } catch (Exception e) {
      logger.error("增加题目失败" + e.getMessage());
    }
    // 保存 Question：若成功则返回0，失败返回1：
    return ZhihuUtil.getJSONString(1, "失败");
  }

  // 问题详情页：
  // 因为要把数据直接返回给前端的上下文所以应传回一个 model；
  // 视频 6：新增了对问题答案的显示：
  @Autowired CommentService commentService;

  @RequestMapping(
      value = "/question/{qid}",
      method = {RequestMethod.GET})
  public String questionDetail(Model model, @PathVariable("qid") int qid) {
    Question question = questionService.selectById(qid);
    model.addAttribute("question", question);
    model.addAttribute("user", userService.getUser(question.getUserId()));

    // 视频 6 新增：
    List<Comment> commentList =
        commentService.selectCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
    // 在页面中显示问题的答案时，不仅要显示答案本身，还要显示答案提交者的用户名、头像等信息：
    // 在向前端传递 复杂的信息（把答案信息和用户信息合在一起传过去） 时，一般使用 viewObject对象：
    // 以便在 前端的 detail.html中使用；
    List<ViewObject> comments = new ArrayList<ViewObject>();
    for (Comment comment : commentList) {
      ViewObject vo = new ViewObject();
      vo.set("comment", comment);
      vo.set("user", userService.getUser(comment.getUserId()));
      comments.add(vo);
    }
    model.addAttribute("comments", comments);
    // 视频 6 END；

    return "detail";
  }
}
