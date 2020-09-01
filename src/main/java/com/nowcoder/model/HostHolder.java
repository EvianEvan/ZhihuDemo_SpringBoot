package com.nowcoder.model;

import org.springframework.stereotype.Component;

// 视频4-1：重点：使用拦截器进行页面访问的控制：

// HostHolder这个对象专门用来存放目前进行访问的用户；
// （通过使用依赖注入来实现对当前用户的共享，所有的页面都可以直接通过注解的方式来引用它）

// 由于多个已登录的用户可能同时访问同一个页面，所以需要使用java中的ThreadLocal来定义各个线程的本地变量；
// ThreadLocal:看起来是一个变量，但实际上每个线程都有一个该变量的复制品，而且可以通过一个公共的接口来访问；
// getUser时会根据当前的线程来找到当前线程所关联的变量副本，底层实现类似于 Map<ThreadID，User>；
@Component
public class HostHolder {
  private static ThreadLocal<User> users = new ThreadLocal<User>();

  public User getUser() {
    return users.get();
  }

  public void setUser(User user) {
    users.set(user);
  }

  public void clear() {
    users.remove();
  }
}
