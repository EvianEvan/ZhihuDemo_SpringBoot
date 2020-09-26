package com.nowcoder.controller;

// 视频 6：评论中心功能 + 站内信页面（包含分页功能）：
// 视频 6： 业务开发 part 2：消息中心（站内信）页面功能：（包含 复杂SQL 和分页功能）

// 本功能更改的文件有
// com/nowcoder/model/Message.java
// com/nowcoder/dao/MessageDAO.java
// com/nowcoder/service/MessageService.java
// com/nowcoder/controller/MessageController.java

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ZhihuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
  private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

  @Autowired HostHolder hostHolder;

  @Autowired MessageService messageService;

  @Autowired UserService userService;

  // 发送私信功能（发新消息的弹窗）：（因为是弹窗所以返回 Json；因为是提交信息，所以用 Post方法）
  @RequestMapping(
      path = {"/msg/addMessage"},
      method = {RequestMethod.POST})
  @ResponseBody
  public String addMessage(
      @RequestParam("toName") String toName, @RequestParam("content") String content) {
    try {

      // 判断是否已经登录；
      if (hostHolder.getUser() == null) {
        return ZhihuUtil.getJSONString(999, "未登录");
      }

      // 若已经登录则继续操作：
      // 首先要取出 toName对应的用户；
      User user = userService.selectByName(toName);
      if (user == null) {
        return ZhihuUtil.getJSONString(1, "用户不存在");
      }
      // 之后构造这个消息；
      Message message = new Message();
      message.setCreatedDate(new Date());
      message.setFromId(hostHolder.getUser().getId());
      message.setToId(user.getId());
      message.setContent(content);
      // 提交保存消息；
      messageService.addMessage(message);
      return ZhihuUtil.getJSONString(0);

    } catch (Exception e) {
      logger.error("发送消息失败" + e.getMessage());
      return ZhihuUtil.getJSONString(1, "发信失败");
    }
  }

  // 1.页面地址映射：私信列表页：(高级SQL)(难重点)
  // SQL:
  // SELECT * FROM message; 和
  // SELECT * FROM message GROUP BY conversation_id;
  // 的区别是：后者会将相同 conversation_id的 数据进行打包，只保留时间最早的一条记录；
  //
  // 复杂 SQL part 1： （ GROUP BY）：
  // GROUP BY默认保留时间最早的一条记录，但私信列表页中需要显示最新的一条记录，所以需要进行排序处理：
  // SELECT * FROM (SELECT * FROM message ORDER BY created_date DESC) tt GROUP BY conversation_id;
  // 上句中，第一层为(SELECT * FROM message ORDER BY created_date DESC) 先将所有数据逆序，
  // 然后在第二层中执行 GROUP BY操作，并输出结果；
  // PS: tt是一个表名，只是为了 外层select语句的语法完整性，才必须写一个随意的表名；
  //
  // 注意：在新版 MySQL中：group by的子查询中 order by 无效，但如加上 limit 则不会被自动优化掉，所以应改为：
  // SELECT * FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt GROUP BY
  // conversation_id
  // 输出结果：
  // '3', '13', '12', 'hi,Evan', '2020-09-20 19:07:36', '0', '12_13'
  //
  // 复杂 SQL part 2： （ count(id) as cnt）：
  // 除了选出所有符合条件的数据之外，还需要将同一个 conversation_id的记录数量统计出来：
  // SELECT *, count(id) as cnt FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id;
  // 输出结果的最后会多出一个 名为 cnt的属性：
  // '3', '13', '12', 'hi,Evan', '2020-09-20 19:07:36', '0', '12_13', '3'
  //
  // 复杂 SQL part 3： （ ORDER BY created_date DESC）：
  // 进行第二次排序，对结果列表进行排序：
  // SELECT *, count(id) as cnt FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id ORDER BY created_date DESC;
  // 在第一层中进行降序排序: (SELECT * FROM message ORDER BY created_date DESC limit 999),
  // 然后在第二层中进行 GROUP BY 后再对选出的结果进行降序排序；
  //
  // 复杂 SQL part 4：（ limit 0,2）：
  // 对结果进行分页：(limit 0,2: 从第 0位置开始，选择两条记录)
  // SELECT *, count(id) as cnt FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id ORDER BY created_date DESC limit 0,2;
  //
  // SQL 总结：
  // SELECT * FROM message;
  //
  // SELECT * FROM message GROUP BY conversation_id;
  //
  // SELECT * FROM (SELECT * FROM message ORDER BY created_date DESC) tt GROUP BY conversation_id;
  //
  // SELECT * FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt GROUP BY
  // conversation_id;
  //
  // SELECT *, count(id) as cnt FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id;
  //
  // SELECT *, count(id) as cnt FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id ORDER BY created_date DESC limit 0,2;

  // 在实际操作中需要注意：（ 来源：com/nowcoder/dao/MessageDAO.java）
  // 由于 count(id) as cnt在结果中添加了cnt属性，但 message model中并没有这个属性，
  // 为了方便操作，可以将 cnt的属性值存到 id属性中，
  // （因为对外界来说并没有用到过 id这个属性（只用到过 conversation_id），id毫无意义，故可覆盖掉该属性)；
  // 所以可以改为：（ count(id) as id）
  // SELECT *, count(id) as id FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id ORDER BY created_date DESC limit 0,2;
  // 其他实施时的注意点见 com/nowcoder/dao/MessageDAO.java；

  // 测试： http://127.0.0.1:8080/msg/list
  @RequestMapping(
      path = {"msg/list"},
      method = {RequestMethod.GET})
  public String getConversationList(Model model) {

    // 获取当前登录用户的信息：
    if (hostHolder.getUser() == null) {
      return "redirect:/reglogin";
    }
    int localUserId = hostHolder.getUser().getId();

    // 取数据：(取 10个作为分页)：复杂 SQL：
    List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);

    // 使用 ViewObject自定义数据类型 向前端传回复杂的数据：
    List<ViewObject> conversations = new ArrayList<ViewObject>();
    for (Message message : conversationList) {
      ViewObject vo = new ViewObject();
      vo.set("message", message);
      // 获取对方用户的 id并获取对方信息，知道对方是谁后，才能展示对话对象的头像等信息；
      int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
      vo.set("user", userService.getUser(targetId));
      vo.set(
          "unread",
          messageService.getConversationUnreadCount(localUserId, message.getConversationId()));
      conversations.add(vo);
    }
    model.addAttribute("conversations", conversations);
    // 返回 html；
    return "letter";
  }

  // 2.页面地址映射：私信详情页：(简单)：
  // 需要传入 conversationId，根据 conversationId来读取所有的相关 message：
  // 注意：无论是A发给B还是B发给A，conversation_id都是一样的，所以显示的是一个完整的对话；
  // 如： http://127.0.0.1:8080/msg/detail?conversationId=12_13
  @RequestMapping(
      path = {"msg/detail"},
      method = {RequestMethod.GET})
  public String getConversationDetail(
      Model model, @RequestParam("conversationId") String conversationId) {
    try {

      // 获取分页后的列表；
      List<Message> messageList = messageService.getConversationDetail(conversationId, 0, 10);
      // 由于展现私信详情时也需要展示相关用户的一些信息（头像等），所以最好使用 viewObject自定义类型来传递这类复杂的数据；
      List<ViewObject> messages = new ArrayList<ViewObject>();
      for (Message message : messageList) {
        ViewObject vo = new ViewObject();
        vo.set("message", message);
        // 获取写信人的信息；
        vo.set("user", userService.getUser(message.getFromId()));
        messages.add(vo);
      }
      model.addAttribute("messages", messages);

    } catch (Exception e) {
      logger.error("获取私信详情页失败" + e.getMessage());
    }

    // 拓展功能：我自己写的：将已读消息的状态置为已读：阅读站内信以后清除未读数字：
    int localUserId = hostHolder.getUser().getId();
    messageService.updateUnread(localUserId, conversationId);

    // 返回 html；
    return "letterDetail";
  }
}
