package com.nowcoder.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

// 叶神 知乎项目高级课 02 代码：AOP：

@Aspect // 启用AOP切面：@Aspect 和 @Component 都要有；// @Component启用IOC进行依赖注入，类似@service；
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    // 利用注解来设置切面：
    @Before("execution(* com.nowcoder.controller.*Controller.*(..))")
    //  不要错写成：@Before("execution(*.com.zhihu.zhihuDemo.controller.*Controller.*(..))"),com前无点！；
    public void beforeMethod(JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : joinPoint.getArgs()) {
            sb.append("Args:" + arg.toString() + "|");
        }
        logger.info("Before Method:" + sb.toString() + new Date()); // 在控制台输出日志；
    }

    // 利用注解来设置切面：
    @After("execution(* com.nowcoder.controller.IndexController.*(..))")
    public void afterMethod() {
        logger.info("After Method:" + new Date());
    }
}
