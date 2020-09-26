package com.nowcoder.dao;

import com.nowcoder.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 视频 6： 业务开发 part 2：消息中心（站内信）功能：（包含 复杂SQL 和分页功能）

// 本功能更改的文件有
// com/nowcoder/model/Message.java
// com/nowcoder/dao/MessageDAO.java
// com/nowcoder/service/MessageService.java
// com/nowcoder/controller/MessageController.java

@Mapper
public interface MessageDAO {
  // 注意加空格；
  String TABLE_NAME = " message ";
  String INSERT_FIELDS = " from_id, to_id, created_date, content, has_read, conversation_id ";
  String SELECT_FIELDS = " id, " + INSERT_FIELDS;

  // 增：增加一条私信消息： 保存成功时返回值大于0；
  // 注意：values 后面的属性必须和 INSERT_FIELDS中的属性一一对应，顺序不能乱；
  @Insert({
    "insert into ",
    TABLE_NAME,
    " (",
    INSERT_FIELDS,
    ") values(#{fromId},#{toId},#{createdDate},#{content},#{hasRead},#{conversationId})"
  })
  int addMessage(Message message);

  // 查 1：查询 私信消息列表 的详情页：
  // 数据库的分页功能：当私信过多时，先选一部分显示出来；（即从第 offset个开始，取 limit个）
  @Select({
    "select ",
    SELECT_FIELDS,
    " from ",
    TABLE_NAME,
    " where conversation_id = #{conversationId} order by created_date desc limit #{offset},#{limit}"
  })
  List<Message> getConversationDetail(
      @Param("conversationId") String conversationId,
      // 分页功能：
      @Param("offset") int offset,
      @Param("limit") int limit);

  // 查 2：查询 私信消息列表 的列表页：(重点：复杂SQL)
  // 要实现的 复杂 SQL：
  // SELECT *, count(id) as cnt FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id ORDER BY created_date DESC limit 0,2;
  // 复杂 SQL的讲解见 com/nowcoder/controller/MessageController.java；
  //
  // 注意：由于 count(id) as cnt在结果中添加了cnt属性，但 message model中并没有这个属性，
  // 为了方便操作，可以将 cnt的属性值存到 id属性中，
  // （因为对外界来说并没有用到过 id这个属性（只用到过 conversation_id），id毫无意义，故可覆盖掉该属性)；
  // 所以可以改为：（ count(id) as id）
  // SELECT *, count(id) as id FROM (SELECT * FROM message ORDER BY created_date DESC limit 999) tt
  // GROUP BY conversation_id ORDER BY created_date DESC limit 0,2;
  //
  // 注意细节：在"select ", INSERT_FIELDS, " from ... 中为 INSERT_FIELDS；
  //
  // 注意：在第一层 order by之前，需要添加 where from_id=#{userId} or to_id=#{userId}，
  // 因为只需要选一个用户（登录用户）相关的记录，不要把所有记录都选出来；
  @Select({
    "SELECT ",
    INSERT_FIELDS,
    " , count(id) as id FROM (SELECT * FROM ",
    TABLE_NAME,
    " where from_id=#{userId} or to_id=#{userId} ORDER BY created_date DESC LIMIT 999) tt "
        + "GROUP BY conversation_id ORDER BY created_date DESC LIMIT #{offset},#{limit}"
  })
  List<Message> getConversationList(
      @Param("userId") int userId,
      // 分页功能：
      @Param("offset") int offset,
      @Param("limit") int limit);

  // 获取未读消息的数量（用户头像上的未读数字小红点）；
  // 需要 userId参数，因为虽然是同一个对话，A、B的未读数量可以不同；
  // 需要获取接收信息的当前登录用户(to_id)的未读数量；
  @Select({
    "select count(id) from ",
    TABLE_NAME,
    "where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"
  })
  int getConversationUnreadCount(
      @Param("userId") int userId, @Param("conversationId") String conversationId);

  // 我自己写的：将已读消息的状态置为已读：阅读站内信以后清除未读数字：
  @Update({
    "update ",
    TABLE_NAME,
    " set has_read=1 where to_id=#{userId} and conversation_id=#{conversationId}"
  })
  int updateUnread(@Param("userId") int userId, @Param("conversationId") String conversationId);
}
