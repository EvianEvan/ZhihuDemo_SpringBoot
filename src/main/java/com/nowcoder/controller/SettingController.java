package com.nowcoder.controller;

import com.nowcoder.service.WendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

// 叶神 知乎项目高级课 02 代码：

@Controller // 不要忘记 @Controller；
public class SettingController {

    // @Autowired：使用IOC注入对象：
    @Autowired WendaService wendaService;

    @RequestMapping(
            path = {"/setting"},
            method = RequestMethod.GET)
    @ResponseBody
    public String setting(HttpSession httpSession) {
        return "Setting OK：" + wendaService.getMessage(886);
    }
}

// 输入：http://127.0.0.1:8080/setting
// 显示：Setting OK：Message from：886
