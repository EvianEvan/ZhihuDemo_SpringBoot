package com.nowcoder.service;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

// 视频 3 代码：首页展示；

// 视频 5 添加代码：主体代码：问题提交按钮功能： + 问题详情页功能；
// （调用的敏感词过滤功能的代码在 SensitiveService.java)；

// 视频 5：多线程介绍：
// 本项目中并没有实际使用多线程，只是在在测试包下做了一个Demo(com/nowcoder/MultiThreadDemo.java)；

// 视频 6： 添加代码：

@Service
public class QuestionService {

  @Autowired QuestionDAO questionDAO;

  @Autowired SensitiveService sensitiveService;

  public List<Question> getLatestQuestions(int userId, int offset, int limit) {
    return questionDAO.selectLatestQuestions(userId, offset, limit);
  }

  // 视频 5 添加代码：问题提交按钮功能 - 问题提交功能：
  // 问题提交函数：提交成功时返回 QuestionID,提交失败时返回 0；

  public int addQuestion(Question question) {

    // 1. 过滤 1 ：在此处对提交内容中的 HTMl标签进行过滤：
    // 为了防范 XSS恶意脚本攻击，必须对所有的 UGC（ User Generated Content，用户产生的数据）进行过滤；
    // 例如将问题提交中的内容写为 <script>var x = document.cookie;alert(x);</script>，
    // 则每次刷新页面时都会有弹窗，如 ticket=3b2fa6c0a37e4842a5；
    // 同理恶意代码也能将 cookie值发给别的网站，造成 token泄露，
    //
    // HtmlUtils是 Spring中的工具类，可将 HTML转换为字符，后续浏览器会将其转移为原值；
    // 例如：
    // 提交问题内容为：<script>var x = document.cookie;alert(x);</script>
    // 数据库中保存的内容是：&lt;script&gt;var x = document.cookie;alert(x);&lt;/script&gt;
    // 浏览器中的显示为：<script>var x = document.cookie;alert(x);</script>
    question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
    question.setContent(HtmlUtils.htmlEscape(question.getContent()));

    // 2. 过滤 2：调用敏感词过滤功能：在此处对提交内容中的敏感词进行过滤：
    // 使用算法：前缀树（字典树）：
    // 算法讲解见视频 5（22分处开始）和 电子版PPT 和 纸质笔记；
    // 算法实现见 com/nowcoder/service/SensitiveService.java；
    question.setTitle(sensitiveService.filter(question.getTitle()));
    question.setContent(sensitiveService.filter(question.getContent()));

    // 3. 正式提交 question，若addQuestion执行成功则返回值大于0；
    return questionDAO.addQuestion(question) > 0 ? question.getId() : 0;
  }

  // 视频5：问题详情页添加：
  public Question selectById(int id) {
    return questionDAO.selectById(id);
  }

  // 视频6： 增加新答案时在 Question表中更新问题的答案数：
  public int updateCommentCount(int id, int count) {
    return questionDAO.updateCommentCount(id, count);
  }
}
