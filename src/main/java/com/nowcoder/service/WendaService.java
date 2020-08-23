package com.nowcoder.service;

import org.springframework.stereotype.Service;

// 叶神 知乎项目高级课 02 代码：

// @Service：用来设置对象的依赖注入：
@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Message from：" + String.valueOf(userId);
    }
}
