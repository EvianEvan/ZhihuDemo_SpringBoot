package com.nowcoder.dao;

import com.nowcoder.model.LoginTicket;
import org.apache.ibatis.annotations.*;

// 视频 4-1：存储用户登陆注册后分发的 token（ticket）；
// (token/session/ticket: 不同地方可能名字不同，但都是一串能标识用户身份的代码)；

// 之所以要单独建一张表来存 ticket（cookie）是因为：
// 同一个用户的多次请求可能打到不同的服务器上，所以需要在不同服务器间共享ticket，所以需要数据库单独建表；
// 类似于共享session（通过数据库（或者redis）同步）的概念；

// 拓展：单点登录的底层思想：
// 一次登录以后，就把token和对应信息放到某一个公共的地方（如session集群），之后所有的服务都可以访问；

// status初始为0，表示有效；改为1后表示 token失效；

@Mapper
public interface LoginTicketDAO {

  // 注意加空格；
  String TABLE_NAME = " login_ticket";
  String INSERT_FIELDS = " user_id, ticket, expired, status ";
  String SELECT_FIELDS = " id, " + INSERT_FIELDS;

  @Insert({
    "insert into",
    TABLE_NAME,
    "(",
    INSERT_FIELDS,
    ") values(#{userId},#{ticket},#{expired},#{status})"
  })
  int addTicket(LoginTicket ticket);

  @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where ticket=#{ticket}"})
  LoginTicket selectByTicket(String ticket);

  @Update({"update ", TABLE_NAME, " set status=#{status} where ticket=#{ticket}"})
  void updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
