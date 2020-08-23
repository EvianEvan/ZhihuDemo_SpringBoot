package com.nowcoder;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.Random;

// 视频3：测试数据库：

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ZhihuApplication.class)

// 测试时前先执行 resources 中的 init-schema.sql;
@Sql("/init-schema.sql")
public class InitDatabaseTests {

    @Autowired
    UserDAO userDAO;

    @Autowired
    QuestionDAO questionDAO;

    @Test
    public void initDatabase() {
        Random random = new Random();

        // 通过dao的方式在数据库中插入数据；
        for (int i = 0; i < 11; i++) {
            User user = new User();
            user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", random.nextInt(1000)));
            user.setName(String.format("USER%d",i));
            user.setPassword("Intel" + i);
            user.setSalt("");
            userDAO.addUser(user);

            user.setPassword("newIntel" + i);
            userDAO.updatePassword(user);

            Question question = new Question();
            question.setCommentCount(i);
            Date date = new Date();
            date.setTime(date.getTime() + 1000*3600*i);
            question.setCreatedDate(date);
            question.setUserId(i+1);
            question.setTitle(String.format("TITLE:%d",i));
            question.setContent(String.format("CONTENT：你今天做了什么？%d", i));
            questionDAO.addQuestion(question);

        }

        // Assert是junit测试框架中的方法；
        Assert.assertEquals("newIntel1", userDAO.selectById(2).getPassword());
        userDAO.deleteById(2);
        Assert.assertNull(userDAO.selectById(2));

        // 测试 Mybatis的 XML使用方式：
        System.out.println(questionDAO.selectLatestQuestions(0,0,10));

    }

}