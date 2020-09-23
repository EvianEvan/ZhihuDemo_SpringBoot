package com.nowcoder.controller;

import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.util.ZhihuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

// 视频 6：评论中心功能 + 站内信页面功能（包含分页功能）：
// 视频 6 - 业务开发 part 1：评论中心功能：

// 视频 6重要总结：
// 在 SpringBoot中进行业务开发的套路（步骤）：
// 通用的业务开发流程（套路） (新功能模块开发的流程)：
// 1. Database Column：设计数据库表：设计表的字段和索引;
// 2. Model：定义模型：属性和数据库的字段相匹配;
// 3. DAO：定义数据库的读取：和数据库进行交互;
// 4. Service：包装服务（作为 DAO（数据库） 和 Controller（业务）之间的中间层）;
// 5. Controller：业务入口;
// 6. Test;（可以用 Postman手动测试，也可以自己写测试用例）
//         （如http://127.0.0.1:8080/addComment ,注意要选择使用POST方法）

// 业务开发 part 1：评论中心功能：
// 统一的评论服务，覆盖所有的实体评论;
//
// 称之为评论中心而不是评论功能的原因：该模块具有可拓展性:
// 如后续其它团队开发了其它的功能则增加一种 entity_type（评论的实体）即可；

// 在评论表中设置 entity_type字段的原因：
// 因为评论可以针对不同的实体（entity）：
// 评论可以针对一个问题（对问题的一个回答），
// 也可以针对另一条评论（对评论的一个回复）；
//
// entity_type将评论表二维化使评论中心可以覆盖所有的实体；
// 第一维表示对应实体的类型（对应的表）（entity_type），
// 第二维表示对应实体的ID（entity_id）；

// 在 SpringBoot中进行业务开发的套路（步骤）（自底向上）：
// 1. 设计并创建数据库表：设计字段，索引：
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
// 此外需要在com/nowcoder/controller/QuestionController.java 中增加问题详情页中对答案（针对问题的评论）的显示功能；
// 因为在本 controller中只是在一个 POST请求中提交了数据：将评论保存到了数据库，并没有显示出来；

@Controller
public class CommentController {
  @Autowired HostHolder hostHolder;

  @Autowired CommentService commentService;

  @Autowired QuestionService questionService;

  private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

  // 增加一条评论，由于是向数据库写入数据所以应使用 POST方法：
  @RequestMapping(
      path = {"/addComment"},
      method = {RequestMethod.POST})
  public String addComment(
      @RequestParam("questionId") int questionId, @RequestParam("content") String content) {
    try {

      // ****** 初始化需要保存的评论：
      Comment comment = new Comment();
      comment.setContent(content);

      if (hostHolder.getUser() == null) {
        // 若用户未登录，可以设置为匿名的用户ID，也可以跳转到登录页面；
        comment.setUserId(ZhihuUtil.ANONYMOUS_USERID);
        // return "redirect:/reglogin";
      } else {
        comment.setUserId(hostHolder.getUser().getId());
      }
      comment.setCreatedDate(new Date());

      // 此处的 comment评论是一个问题的答案，所以针对的实体是一个问题；
      // 在com/nowcoder/model/EntityType.java中设置了 Comment表中的实体类别：
      // 实体类别有两种：一条评论 既可以针对一个问题（作为一条答案） 也可以针对另一条评论（作为评论的回复）；
      comment.setEntityType(EntityType.ENTITY_QUESTION);
      // 设置实体的 ID，即 question的 ID：
      comment.setEntityId(questionId);
      // ****** 初始化需要保存的评论：END

      // 保存评论，且在保存后及时更新 Question表中的 commentCount（评论数）数据：
      // （Question表中的 commentCount会在首页中问题下的评论数部分进行显示）
      // 此处可以合并为一个数据库中的事务：（需要操作两次（两个数据库表），且实为一个原子性操作）；
      // 或者最好是利用 Redis实现为一个异步的操作（因为两个操作其实不需要同步，第二个更新的操作相对于存数据的操作可以稍稍滞后）；
      // 1.在 comment表中保存评论，查询某个实体下的评论总数（即获取最新的 commentCount数据）；
      commentService.addComment(comment);
      int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
      // 2.在 question表中进行更新；
      questionService.updateCommentCount(comment.getEntityId(), count);

    } catch (Exception e) {
      logger.error("增加评论失败： " + e.getMessage());
    }
    // 评论添加成功后跳转到当前问题的页面：
    return "redirect:/question/" + questionId;
  }
}
