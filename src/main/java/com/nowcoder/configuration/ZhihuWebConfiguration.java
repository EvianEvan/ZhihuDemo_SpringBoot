package com.nowcoder.configuration;

import com.nowcoder.interceptor.LoginRequiredInterceptor;
import com.nowcoder.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

// 视频4-1：重点：使用拦截器（interceptor）进行页面访问的控制：

// 拦截器的注册（配置）：通过拓展接口的方式来注册拦截器；
// 拦截器的实现在com/nowcoder/interceptor/PassportInterceptor.java；

@Component
public class ZhihuWebConfiguration extends WebMvcConfigurerAdapter {

  @Autowired PassportInterceptor passportInterceptor;

  @Autowired LoginRequiredInterceptor loginRequiredInterceptor;

  // 重写方法：注册自定义的拦截器：（右键选 Generate再选 Override Methods即可选择要重写的方法）
  @Override
  public void addInterceptors(InterceptorRegistry registry) {

    // 正式注册第一个拦截器：
    registry.addInterceptor(passportInterceptor);

    // 正式注册和配置第二个拦截器：
    // (注意先后顺序:第二个拦截器的实现中用到了第一个拦截器中 hostHolder保存的 user数据，所以必为第二个）;
    // (注意：只有访问用户的个人页面(/user/*)时 才需要对当前访问用户进行未登录检测并进行跳转);
    registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/user/*");

    super.addInterceptors(registry);
  }
}
