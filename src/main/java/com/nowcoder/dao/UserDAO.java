package com.nowcoder.dao;

import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;

// 视频3：Mybatis的使用(注解的方式)：
// (注意：DAO是一些interface接口，mybatis会自动实现一些代理类)

// @Mapper 是 Mybatis中的注解；
@Mapper
public interface UserDAO {

  // 注意加空格；
  String TABLE_NAME = " user ";
  String INSERT_FIELDS = " name, password, salt, head_url ";
  String SELECT_FIELDS = " id, " + INSERT_FIELDS;

  // 注意：#{}中表示 addUser(User user) 中 user的数据，
  // 因此应为 User 中的属性 headUrl 而不是数据库中的属性 head_url；
  // 而sql语句中应与数据库属性保持一致为 head_url；
  @Insert({
    "insert into ",
    TABLE_NAME,
    "(",
    INSERT_FIELDS,
    ") values (#{name},#{password},#{salt},#{headUrl})"
  })
  int addUser(User user);

  @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
  User selectById(int id);

  @Update({"update", TABLE_NAME, "set password=#{password} where id=#{id}"})
  void updatePassword(User user);

  @Delete({"delete from ", TABLE_NAME, "where id = #{id}"})
  void deleteById(int id);
}
