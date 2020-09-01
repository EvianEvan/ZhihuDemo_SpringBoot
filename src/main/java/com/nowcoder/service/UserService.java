package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.User;
import com.nowcoder.util.ZhihuMD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

// 视频3 代码：首页展示：
// 视频4-1 添加代码；
@Service
public class UserService {

  @Autowired UserDAO userDAO;

  @Autowired LoginTicketDAO loginTicketDAO;

  @Autowired LoginTicketService loginTicketService;

  public User getUser(int id) {
    return userDAO.selectById(id);
  }

  //
  // 以下为视频 4-1添加：
  // 函数：注册新用户：
  // 注册成功时直接返回一个空的 map，注册失败则可以添加一些失败信息；
  public Map<String, String> register(String username, String password) {
    Map<String, String> map = new HashMap<String, String>();

    // 判断用户名和密码的格式是否合规；
    // 检查一个字符串既不是null串也不是空串: username != null && username.length() != 0 ;
    if (username == null || username.length() == 0) {
      map.put("msg", "用户名不能为空！");
      return map;
    }
    if (password == null || password.length() == 0) {
      map.put("msg", "密码不能为空！");
      return map;
    }

    // 判断用户名是否已经存在：
    User user = userDAO.selectByName(username);
    if (user != null) {
      map.put("msg", "用户名已经被注册了！");
      return map;
    }

    // 如没有出现前面的问题，则进入正式部分：把输入的注册数据存入数据库：
    user = new User();
    user.setName(username);
    // 生成一个随机字符作为 salt属性；
    user.setSalt(UUID.randomUUID().toString().substring(0, 5));
    // 把 密码 和 salt 连在一起后再对密码进行MD5加密，以提高密码的破解难度；
    user.setPassword(ZhihuMD5Util.MD5(password + user.getSalt()));
    String head =
        String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
    user.setHeadUrl(head);
    userDAO.addUser(user);

    // 成功时返回一个带有 token（新生成的ticket）的map：
    String ticket = loginTicketService.addLoginTicket(user.getId());
    map.put("ticket", ticket);
    return map;
  }

  //
  // 函数：登录：（与注册类似）：
  // 成功时直接返回一个空的 map，失败则可以添加一些失败信息；
  public Map<String, String> login(String username, String password) {
    Map<String, String> map = new HashMap<String, String>();

    // 判断用户名和密码的格式是否合规；
    if (username == null || username.length() == 0) {
      map.put("msg", "用户名不能为空！");
      return map;
    }
    if (password == null || password.length() == 0) {
      map.put("msg", "密码不能为空！");
      return map;
    }

    // 判断用户是否已经存在：
    User user = userDAO.selectByName(username);
    if (user == null) {
      map.put("msg", "用户名不存在！");
      return map;
    }

    // 判断密码是否正确：
    if (!ZhihuMD5Util.MD5(password + user.getSalt()).equals(user.getPassword())) {
      map.put("msg", "密码错误！");
      return map;
    }

    // 成功时返回一个带有 token（新生成的ticket）的map：
    String ticket = loginTicketService.addLoginTicket(user.getId());
    map.put("ticket", ticket);
    return map;
  }

  //
  // 函数：当前用户退出登录：（只需让ticket失效(将status设为1)）：
  // 用户退出时建议不删除或覆盖原有数据，而是只置为1，因为这些都是用户的习惯数据，有数据挖掘的价值；
  public void logout(String ticket) {
    loginTicketDAO.updateStatus(ticket, 1);
  }
  // 视频 4-1添加：END；

}
