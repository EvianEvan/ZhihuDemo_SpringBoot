package com.nowcoder.dao;

import com.nowcoder.model.Question;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 视频3：Mybatis的使用(注解 或 XML)：
// (注意：DAO是一些interface接口，mybatis会自动实现一些代理类)

// @Mapper 是 Mybatis中的注解；
@Mapper
public interface QuestionDAO {

  // 注意加空格；
  String TABLE_NAME = " question ";
  String INSERT_FIELDS = " title, content, created_date, user_id, comment_count";
  String SELECT_FIELDS = " id, " + INSERT_FIELDS;

  // Mybatis的使用方式1：注解：
  // 注意：#{}中表示 model 中的数据，
  // 因此应为 Question 中的属性 createdDate 而不是数据库中的属性 created_date；
  // 而sql语句中应与数据库属性保持一致为created_date；
  @Insert({
    "insert into ",
    TABLE_NAME,
    " (",
    INSERT_FIELDS,
    ") values(#{title},#{content},#{createdDate},#{userId},#{commentCount})"
  })
  int addQuestion(Question question);

  // Mybatis的使用方式2：使用XML配置文件：
  // （ XML文件位置：main/resources/com/nowcoder/dao/QuestionDAO.xml）：
  // 三个参数需要和 XML中的三个参数一一匹配；
  List<Question> selectLatestQuestions(
      @Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
}
