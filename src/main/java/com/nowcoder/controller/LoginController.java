package com.nowcoder.controller;

import com.nowcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

// 视频 4-1代码：用于控制用户的注册和登录页：
// 视频 4-2 添加了部分代码；

@Controller
public class LoginController {

  @Autowired UserService userService;

  private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

  //
  // 登录与注册的入口页面：
  // 测试： http://127.0.0.1:8080/reglogin；
  // 视频 4-2 添加了 next 相关代码；
  // next 是在 实现未登录时跳转登录功能的拦截器 中传回的参数；
  // （com/nowcoder/interceptor/LoginRequiredInterceptor.java）

  //  在此处把 URL中可能存在的next参数 传给浏览器后，前端又把 next存在了 html中
  // （存在 templates/reglogin.html中 regloginform下的
  //   <input type="hidden" name="next" value="$!{next}"/> 中，
  //   因此之后当前端提交 form表单(点击登录/注册)时又会一同传回 next参数）；

  // 因为是GET方法，所以 @RequestParam中获取到的是URL路径中的参数，而不是 form提交的参数；
  @RequestMapping(
      path = {"/reglogin"},
      method = {RequestMethod.GET})
  public String reg(Model model, @RequestParam(value = "next", required = false) String next) {
    model.addAttribute("next", next);
    return "reglogin";
  }

  //
  // 注册：
  // （注意：此处没有对应浏览器中的地址路径，
  // 而是对应了/reglogin页 html中的form表单的提交请求地址，
  // 详见 templates/reglogin.html）
  // 测试： http://127.0.0.1:8080/reglogin；
  @RequestMapping(
      path = {"/reg"},
      method = {RequestMethod.POST})
  public String reg(
      Model model,
      @RequestParam("username") String username,
      @RequestParam("password") String password,
      // 视频 4-2：这些都是前端通过form提交的参数；
      @RequestParam(value = "next", required = false) String next,
      // 视频 4-2：END；
      @RequestParam(value = "rememberme", defaultValue = "false") boolean remember,
      HttpServletResponse response) {
    try {
      // 注册：
      // 注册成功时userService.register将返回一个空的 map，注册失败将返回失败信息；
      Map<String, String> map = userService.register(username, password);

      // 在浏览器保存注册成功后生成的 ticket；
      // 通过HttpServletResponse来向浏览器下发UserService中返回的用户ticket；
      // （通过response把 ticket写入cookie中）；
      if (map.containsKey("ticket")) {
        Cookie cookie = new Cookie("ticket", map.get("ticket"));
        cookie.setPath("/");
        response.addCookie(cookie);
      }

      if (map.containsKey("msg")) {
        // 把注册失败的原因返回给前端：
        model.addAttribute("msg", map.get("msg"));
        // 注册失败后跳转到登录注册入口页页；
        return "reglogin";
      }

      // 注册成功则跳回首页；
      // 视频 4-2：成功后返回登录前页面；
      if (next != null && !next.equals("")) {
        return "redirect:" + next;
      }
      // 视频 4-2：END；
      return "redirect:/";

    } catch (Exception e) {
      logger.error("注册异常" + e.getMessage());
      // 注册异常后也跳转到登录注册入口页；
      return "reglogin";
    }
  }

  //
  // 登录：（与注册类似）
  // （注意：此处没有对应浏览器中的地址路径，
  // 而是对应了/reglogin页 html中的form表单的提交请求地址，
  // 详见 templates/reglogin.html）
  // 测试： http://127.0.0.1:8080/reglogin；
  // 若在reglogin页面中选择了‘记住我’选项，则将cookie的有效期设的长一点；
  @RequestMapping(
      path = {"/login"},
      method = {RequestMethod.POST})
  public String login(
      Model model,
      @RequestParam("username") String username,
      @RequestParam("password") String password,
      // 视频 4-2：这些都是前端通过form提交的参数；
      @RequestParam(value = "next", required = false) String next,
      // 视频 4-2：END；
      @RequestParam(value = "rememberme", defaultValue = "false") boolean rememberme,
      HttpServletResponse response) {
    try {
      // 登录：成功时userService.login将返回一个空的 map，失败将返回失败信息；
      Map<String, String> map = userService.login(username, password);

      // 在浏览器保存登录成功后生成的 ticket；
      // 通过HttpServletResponse来向浏览器下发UserService中返回的用户ticket；（通过response把ticket写入cookie中）；
      if (map.containsKey("ticket")) {
        Cookie cookie = new Cookie("ticket", map.get("ticket"));
        cookie.setPath("/");
        response.addCookie(cookie);
      }

      if (map.containsKey("msg")) {
        // 把失败的原因返回给前端：
        model.addAttribute("msg", map.get("msg"));
        // 失败后跳转到登录注册入口页；
        return "reglogin";
      }

      // 成功则跳回首页；
      // 视频 4-2：成功后返回登录前页面；
      if (next != null && !next.equals("")) {
        return "redirect:" + next;
      }
      // 视频 4-2：END；
      return "redirect:/";

    } catch (Exception e) {
      logger.error("登录异常" + e.getMessage());
      // 异常后也跳转到登录注册入口页；
      return "reglogin";
    }
  }

  //
  // 当前用户退出：只需将cookies中的 ticket删除即可；
  // （注意：此处没有对应浏览器中的地址路径，
  // 而是对应了 header.html中的退出按钮（<a href="/logout" id=":4">），
  // 详见 templates/header.html）
  // 测试： http://127.0.0.1:8080/；
  @RequestMapping(
      path = {"/logout"},
      method = {RequestMethod.GET})
  public String logout(@CookieValue("ticket") String ticket) {
    userService.logout(ticket);
    // 退出当前用户后返回首页：
    return "redirect:/";
  }
}
