package com.nowcoder.interceptor;

import com.nowcoder.model.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 视频 4-2 重点：
// 第二个拦截器：使用拦截器（interceptor）进行页面访问的控制 2：
// 利用 当前访问用户的身份信息 实现对未登录用户的登录页跳转（同时要求登录/注册后需返回登录前访问的页面）；

// 第二个拦截器的实现中用到了第一个拦截器在 hostHolder中保存的 user数据(即当前访问用户的身份信息)，所以必为第二个注册；

// 注意：只有访问用户的个人页面(/user/*)时 才需要对当前访问用户进行未登录检测并进行跳转;
// 例如：http://127.0.0.1:8080/user/8 ；
// 这一设置需在 com/nowcoder/configuration/ZhihuWebConfiguration.java中进行配置；
// PS:在使用第二个拦截器之前，用户的个人页面 无论是否已经登录/无论是谁 都可以随意访问，没有进行任何检测；

//
// 实现未登录跳转功能时涉及到的文件较多，且需要前后端配合：涉及文件如下：
// 1.LoginRequiredInterceptor拦截器在访问个人页面时拦截未登录用户，
//   并跳转到登录页面（/reglogin）（在跳转时把当前页面的地址作为 next参数放入URL中）：
// com/nowcoder/interceptor/LoginRequiredInterceptor.java

// 2.把 URl中的 next 添加到 model中传给浏览器前端；
// com/nowcoder/controller/LoginController.java

// 3.前端把 next参数保存在一个 HTML input元素中，后续输入完成点击 登录/注册 提交 form时会将 next传回后端：
// templates/reglogin.html

// 4.利用提交的 form中的 next参数在成功 登录/注册 后跳回登录前页面；
// com/nowcoder/controller/LoginController.java

// 功能完成；

// 注意：目前的跳转实现并不安全，应该增加一些对 next参数的判断（如正则表达式等），保证 next只能在站内跳转，
// 否则若黑客手动更改 next参数则会跳转到不安全网站；
// 如改为 http://127.0.0.1:8080/reglogin?next=https://www.baidu.com/ 则登录/注册成功后会跳转到百度；

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

  @Autowired HostHolder hostHolder;

  // 在访问用户的个人页面(/user/*)时 判断用户是否已经登录，若未登录则跳转到登录页面；
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // 利用 当前访问用户的身份信息 实现未登录用户的跳转登录，
    // 同时把当前页面地址作为参数传进 跳转到的登录页面的URL 中，为登录后的跳回做准备;
    if (hostHolder.getUser() == null) {
      // 例如：http://127.0.0.1:8080/reglogin?next=/user/8 ；
      response.sendRedirect("/reglogin?next=" + request.getRequestURI());
    }
    // 返回 true则执行完 preHandle之后会执行后面的 controller(即 LoginController中的reg)；
    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {}

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {}
}
