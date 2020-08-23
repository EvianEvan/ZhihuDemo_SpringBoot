package com.nowcoder.controller;

import com.nowcoder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

// 必看！！！重要！！！
// 叶神 知乎项目高级课 02 代码主体：

// 老的首页：
// 注意：在视频3之后中此controller已被注释！不再生效！；
// @Controller  // 已被注释！
public class IndexController {

  private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

  // 1.地址映射：
  // @RequestMapping("/")
  // method = {RequestMethod.GET}:只接受GET类型的HTTP请求；
  @RequestMapping(
      path = {"/", "/index"},
      method = {RequestMethod.GET})
  // @ResponseBody 表示直接返回文本字符串，若将ResponseBody注释掉则会去resources下的templates中去寻找模板；
  @ResponseBody
  // v1 原版
  // public String index() {
  //    return "Hello NewCoder";
  //  }

  // v2 在重定向时，利用HttpSession传递信息
  // (PS：在关闭所有浏览器Tab页面后session中数据仍存在，只有重启浏览器后session中数据才消失)；
  // cookie中保存的sessionID只有重启浏览器后才会改变，关闭tab后不会变；
  public String index(HttpSession httpSession) {
    logger.info("访问主页"); // 在控制台输出日志；
    if (httpSession.getAttribute("RedirectMsg:") != null) {
      return "Hello NewCoder "
          + httpSession.getAttribute("RedirectMsg:"); // 注意勿漏RedirectMsg:中的：符号！；
    } else {
      return "Hello NewCoder ";
    }
  }

  // 2.参数提取：
  // @PathVariable提取路径中的参数，@RequestParam提取请求中的参数（问号后面的参数）；
  // eg1: http://127.0.0.1:8080/profile/admin/666?page=22&key=23
  //     页面显示：This is Profile Page of admin/666/22, key is 23
  // eg2: http://127.0.0.1:8080/profile/admin/666?
  //     页面显示：This is Profile Page of admin/666/1, key is null
  @RequestMapping(path = {"/profile/{groupId}/{userId}"})
  @ResponseBody
  public String profile(
      @PathVariable("groupId") String group,
      @PathVariable("userId") int userId,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "key", required = false) String key) {
    return String.format("This is Profile Page of %s/%d/%d, key is %s", group, userId, page, key);
  }

  // 3.使用返回的模板页面：
  @RequestMapping(
      path = {"/vm"},
      method = {RequestMethod.GET})
  public String template(Model model) {
    model.addAttribute("value1", "vvv");
    List<String> colors = Arrays.asList(new String[] {"Red", "Green", "Blue"});
    model.addAttribute("colors", colors);

    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < 4; i++) {
      map.put(String.valueOf(i), String.valueOf(i * i));
    }
    model.addAttribute("map", map);
    model.addAttribute("user", new User("LEE"));

    return "home";
  }

  // 4.HTTP中的 Request和 Response：
  @RequestMapping(path = {"/request"})
  @ResponseBody
  public String request(
      HttpServletRequest request, HttpServletResponse response, HttpSession httpsession) {

    // 打印请求头：
    StringBuilder sb = new StringBuilder();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      sb.append(name + ":" + request.getHeader(name) + "<br>");
    }

    //  获取cookie：
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        sb.append("Cookie:" + cookie.getName() + " value:" + cookie.getValue()).append("<br>");
      }
    }

    // 打印request内容：
    sb.append(request.getMethod()).append("<br>");
    sb.append(request.getQueryString()).append("<br>");
    sb.append(request.getPathInfo()).append("<br>");
    sb.append(request.getRequestURI()).append("<br>");

    // 添加response内容：
    response.addHeader("NewHeader", "wang");
    response.addCookie(new Cookie("Coder", "timo"));

    return sb.toString();
  }

  // 5.重定向：
  // v1 默认为302临时重定向；
  //  @RequestMapping(
  //      path = {"/redirect"},
  //      method = {RequestMethod.GET})
  //  public String redirect() {
  //    return "redirect:/";
  //  }
  //
  // v2 设置为301永久重定向；
  @RequestMapping(
      path = {"/redirect/{code}"},
      method = {RequestMethod.GET})
  public RedirectView redirect(@PathVariable("code") String code, HttpSession httpSession) {
    httpSession.setAttribute("RedirectMsg:", "jumped from redirect."); // 注意勿漏RedirectMsg:中的：符号！；
    RedirectView red = new RedirectView("/", true);
    if (code.equals("permanent")) {
      red.setStatusCode(HttpStatus.MOVED_PERMANENTLY); // 设置为301重定向；
    }
    return red;
  }
  // 输入：
  // http://127.0.0.1:8080/redirect/permanent
  // 显示：
  //  Hello NewCoder jumped from redirect.

  // 6.异常的捕获：（自定义异常页面）
  // 返回一个自定义页面捕获并显示Spring不能处理的异常
  // （Spring MVC外的Exception或Spring MVC没有处理的Exception）；
  @ExceptionHandler
  @ResponseBody
  public String error(Exception e) {
    return "捕获到异常:" + e.getMessage();
  }
  //
  // 实验：抛出一个异常：
  @RequestMapping(
      path = {"/admin"},
      method = {RequestMethod.GET})
  @ResponseBody
  public String admin(@RequestParam("name") String name) {
    if ("admin".equals(name)) {
      return "Hello admin!";
    }
    throw new IllegalArgumentException("参数不对");
  }

  // 输入：
  // http://127.0.0.1:8080/admin?name=admin
  // 显示：
  // Hello admin!

  // 输入：
  // http://127.0.0.1:8080/admin?name=ad
  // 显示：
  // 捕获到异常:参数不对

  // 输入：
  // http://127.0.0.1:8080/ad
  // 显示：
  // Whitelabel Error Page
  // This application has no explicit mapping for /error, so you are seeing this as a fallback.
  // Thu Aug 13 21:18:39 CST 2020
  // There was an unexpected error (type=Not Found, status=404).
  // No message available

  // 7.Ioc:依赖注入：
  // 传统方法：使用时创建一个对象，再去创建这个对象所依赖的对象，依次有序创建对象，顺序不能乱；
  // Ioc:构造一张依赖网，在使用时直接注入对象，无需再new对象；
  //
  // 优势：无需关注这些对象变量的初始化，
  // 只需通过注解（@Service设置对象来源、@Autowired直接使用对象）的方式来注入Bean池中的对象；
  // 思想：设计模式中的享元模式；
  //
  // 示例见：（定义一些service对象，然后在controller里面通过Ioc来直接使用这些对象）
  // com/zhihu/zhihuDemo/service/WendaService.java  @Service设置对象来源；
  // com/zhihu/zhihuDemo/controller/SettingController.java @Autowired注入对象；

  // 8.AOP：面向切面编程：所有业务都要处理的业务：、
  // (例如通过注解来插入需要在所有的controller里都插入的代码)
  // 示例见：
  // com/zhihu/zhihuDemo/aspect/LogAspect.java
  // @Aspect @Component来启用切面；@Aspect 和 @Component 都要有；
  // @Before @After设置切面；

  //  输入：
  //  http://127.0.0.1:8080/request?user=wang
  //
  //  页面显示内容：
  //  host:127.0.0.1:8080
  //  connection:keep-alive
  //  cache-control:max-age=0
  //  dnt:1
  //  upgrade-insecure-requests:1
  //  user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)
  //  Chrome/84.0.4147.105 Safari/537.36
  //
  // accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
  // sec-fetch-site:none
  // sec-fetch-mode:navigate
  // sec-fetch-user:?1
  // sec-fetch-dest:document
  // accept-encoding:gzip, deflate, br
  // accept-language:zh-CN,zh;q=0.9,en;q=0.8
  // cookie:Coder=timo; JSESSIONID=7AB54D33CFA9B0DD81DF6CC11846C2C9
  // Cookie:Coder value:timo
  // Cookie:JSESSIONID value:7AB54D33CFA9B0DD81DF6CC11846C2C9
  // GET
  // user=wang
  // null
  /// request

}
