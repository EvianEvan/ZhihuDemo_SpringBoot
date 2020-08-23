package com.nowcoder.controller;

// 叶神 知乎项目高级课 视频3 ：1时15分：创建新首页：

import com.nowcoder.model.Question;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

// 新的首页：
@Controller
public class HomeController {
  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  @Autowired UserService userService;

  @Autowired QuestionService questionService;

  // 显示主页内容：
  // 测试：http://127.0.0.1:8080/
  @RequestMapping(
      path = {"/", "/index"},
      method = {RequestMethod.GET})
  public String index(Model model) {
    model.addAttribute("vos", getQuestions(0, 0, 10));
    return "index";
  }

  // 功能子函数：获取问题列表：
  private List<ViewObject> getQuestions(int userId, int offset, int limit) {
    List<Question> questionList = questionService.getLatestQuestions(userId, offset, limit);
    // 使用 viewobject对象可以存放任何东西；
    // 最终把 vos传给前端；在html中使用 velocity以 $vo.question的方式来使用数据；
    List<ViewObject> vos = new ArrayList<ViewObject>();
    for (Question question : questionList) {
      ViewObject vo = new ViewObject();
      vo.set("question", question);
      vo.set("user", userService.getUser(question.getUserId()));
      vos.add(vo);
    }
    return vos;
  }

  // 显示用户页内容：
  // 测试：http://127.0.0.1:8080/user/11
  @RequestMapping(
      path = {"/user/{userId}"},
      method = {RequestMethod.GET})
  public String userIndex(Model model, @PathVariable("userId") int userId) {
    model.addAttribute("vos", getQuestions(userId, 0, 10));
    return "index";
  }
}
