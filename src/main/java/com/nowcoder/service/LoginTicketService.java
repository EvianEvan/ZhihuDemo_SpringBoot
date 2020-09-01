package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.model.LoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

//  视频4-1：用来读取 LoginTicketDAO：
//  在 UserService中使用到了 此Service；
@Service
public class LoginTicketService {

  @Autowired private LoginTicketDAO loginTicketDAO;

  // 用户登录或注册后分发一个和用户名（userId）相关联的 ticket，存入数据库并返回；
  public String addLoginTicket(int userId) {
    LoginTicket loginTicket = new LoginTicket();
    loginTicket.setUserId(userId);
    Date now = new Date();
    // 有效期设为10天（用毫秒（1秒=1000 毫秒）数表示）；
    now.setTime(1000 * 3600 * 24 * 10 + now.getTime());
    loginTicket.setExpired(now);
    // status默认初始为0，表示ticket有效，
    // 用户登出时status设为1表示ticket失效；
    loginTicket.setStatus(0);
    // 设置ticket时，把随机字符串UUID中的-全部替换掉；
    loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));

    // 把ticket存入数据库；
    loginTicketDAO.addTicket(loginTicket);

    return loginTicket.getTicket();
  }
}
