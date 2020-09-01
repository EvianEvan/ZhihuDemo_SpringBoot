package com.nowcoder.interceptor;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

// 视频 4-1：重点：
// 第一个拦截器：使用拦截器（interceptor）进行页面访问的控制 1：
// 识别并共享 当前访问用户的身份信息；(利用一个ThreadLocal的变量来实现共享)；

// 拦截器类似于切面的思想，在原有的业务链路上留一些口子，让后面拓展的业务人员可以插进来做一些事情；
// 拦截器（interceptor）和切面（aspect）的主要区别：面向的对象不同（拦截器面向所有的http请求）；
// 使用切面也可以实现拦截器，但很麻烦，因为要手动获取 HttpServletRequest等对象；

// 拦截器的自定义实现：
// 拦截器的注册在com/nowcoder/configuration/ZhihuWebConfiguration.java中；

// 通过实现 HandlerInterceptor接口来自定义拦截器，
// 接口内含有preHandle、postHandle、afterCompletion三个回调函数；

// 所有的 controller都会被拦截器拦截（在处理所有请求时都让拦截器处理一遍）；
// （preHandle是指在你开始做任何事情之前都会被拦截器拦截，
//  因为在任何操作之前都必须先确定当前访问用户的权限）；

// @Component：对该对象注册依赖注入；
@Component
public class PassportInterceptor implements HandlerInterceptor {

  @Autowired LoginTicketDAO loginTicketDAO;
  @Autowired UserDAO userDAO;
  @Autowired HostHolder hostHolder;

  // preHandle：在Http请求开始之前就会调用这个函数，
  //            若返回 false则整个请求直接结束，不会继续执行下去（不会进入任何controller等）；
  //            可以用来判断用户有没有访问页面的权限；
  //            若返回 true则执行完 preHandle之后会执行后面的 controller；
  // 在拦截器最早的地方（preHandle）就把当前访问用户存入HostUser，
  // 所以后面所有的 controller、service等都可以通过 hostHolder.getUser()直接获取当前访问用户；

  // 本preHandle实现功能：
  // 用来判断当前访问用户的身份，判断后把用户身份存到了一个ThreadLocal的变量里面（HostHolder），
  // 以使后面所有的 controller、service等都可以直接获取当前访问用户；
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String ticket = null;

    // 获取cookie中的 ticket；
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals("ticket")) {
          ticket = cookie.getValue();
          break;
        }
      }

      // 验证 ticket是否已经失效；
      if (ticket != null) {
        LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
        if (loginTicket == null
            || loginTicket.getExpired().before(new Date())
            || loginTicket.getStatus() != 0) {
          return true; // 若已失效则直接 return退出当前函数（preHandle）；
        }
        User user = userDAO.selectById(loginTicket.getUserId());
        // 成功得到用户后，把当前访问用户保存到 hostUser（一个ThreadLocal的变量中），
        // 进而在所有页面间共享当前访问用户；
        hostHolder.setUser(user);
      }
    }

    return true;
  }

  // postHandle：可以在渲染之前把一些数据传进去；
  // 本preHandle实现功能：
  // 在渲染之前把当前访问对象user放到velocity的上下文；
  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    // 若modelAndView不为空，则在渲染之前把用户放进去，这样就可以在所有页面的模板里（使用velocity）直接访问变量；
    // （modelAndView指 Model和模板文件（view））；
    if (modelAndView != null) {
      modelAndView.addObject("user", hostHolder.getUser());
    }
  }

  // afterCompletion：在请求结束（全部渲染完）之后，做一些删除之类的操作；
  // 结束后把变量清除掉，以免 hostHolder中 ThreadLocal里的用户越来越多；
  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    hostHolder.clear();
  }
}
