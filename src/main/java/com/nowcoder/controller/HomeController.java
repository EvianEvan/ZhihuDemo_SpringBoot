package com.nowcoder.controller;

// 叶神 知乎项目高级课 视频3 ：1时15分：创建新首页：
// 新的首页：

// 视频 6重要总结：
// 在 SpringBoot中进行业务开发的套路（步骤）（自底向上）：
// 通用的业务开发流程（套路） (新功能模块开发的流程)：
// 1. Database Column：设计数据库表：设计表的字段和索引;
// 2. Model：定义模型：属性和数据库的字段相匹配;
// 3. DAO：定义数据库的读取：和数据库进行交互;
// 4. Service：包装服务（作为 DAO（数据库） 和 Controller（业务）之间的中间层）;
// 5. Controller：业务入口;
// 6. Test;（可以用 Postman手动测试，也可以自己写测试用例）
//         （如http://127.0.0.1:8080/addComment ,注意要选择使用POST方法）

// 业务开发的套路（步骤）：
// 1. 创建数据库表：设计字段，索引：
// 其中字段 status用来标记记录是否已被删除，1表示这条记录已被删除，默认正常状态为0；
// CREATE TABLE `zhihu`.`comment` (
//  `id` INT NOT NULL AUTO_INCREMENT,
//  `user_id` INT NOT NULL,
//  `content` TEXT NOT NULL,
//  `created_date` DATETIME NOT NULL,
//  `entity_type` INT NOT NULL,
//  `entity_id` INT NOT NULL,
//  `status` INT NOT NULL DEFAULT 0,
//  PRIMARY KEY (`id`),
//  INDEX `entity_index` (`entity_type` ASC, `entity_id` ASC) INVISIBLE,
//  INDEX `user_index` (`user_id` ASC) VISIBLE);

// 2. 创建模型：Model：（ MyBatis基于此文件和数据库进行匹配）
// 新建 com/nowcoder/model/Comment.java 和 com/nowcoder/model/EntityType.java：
// PS：模型的属性需要和数据库的字段相互匹配：

// 3. 创建 DAO：用于操作数据库；
// 新建 com/nowcoder/dao/CommentDAO.java：定义SQL语句；

// 4. 创建 service：包装服务，作为 DAO（数据库）和 Controller（业务）之间的中间层；
// 新建 com/nowcoder/service/CommentService.java；

// 5. 创建 controller：映射路径，设置业务逻辑的入口：

import com.nowcoder.model.HostHolder;
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

@Controller
public class HomeController {
  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  // 使用 @Component等注解来启用对象的依赖注入，使用 @Autowired注解来使用对象；
  @Autowired UserService userService;

  @Autowired QuestionService questionService;

  // 视频4-1测试：测试拦截器：
  // （在com/nowcoder/interceptor/PassportInterceptor.java中的
  //   自定义preHandle拦截器中把user对象保存到了hostHolder中，
  //   因此随处(所有的controller、service等)都可以获取到当前访问的user对象；）
  @Autowired HostHolder hostHolder;
  // 视频4-1测试：测试拦截器：END；

  // 显示主页内容：
  // 测试：http://127.0.0.1:8080/
  @RequestMapping(
      path = {"/", "/index"},
      method = {RequestMethod.GET})
  public String index(Model model) {
    model.addAttribute("vos", getQuestions(0, 0, 12));

    // 视频4-1测试：测试拦截器：
    hostHolder.getUser();
    // 视频4-1测试：测试拦截器：END；

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
