package com.nowcoder.dao;

import com.nowcoder.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 视频 6：评论中心功能代码：

// @Mapper是Mybatis中的注解；
@Mapper
public interface CommentDAO {

  // 注意加空格；
  String TABLE_NAME = " comment ";
  String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status ";
  String SELECT_FIELDS = " id, " + INSERT_FIELDS;

  // 增：增加一条评论：（如为一个问题添加一条回答）
  // 保存成功时返回值大于0；
  @Insert({
    "insert into ",
    TABLE_NAME,
    " (",
    INSERT_FIELDS,
    ") values(#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status})"
  })
  int addComment(Comment comment);

  // 查 1：根据一个实体来选出它所有的评论：（如查询一个问题下的所有回答）
  @Select({
    "select ",
    SELECT_FIELDS,
    " from ",
    TABLE_NAME,
    " where entity_id = #{entityId} and entity_type = #{entityType} order by created_date desc"
  })
  List<Comment> selectCommentsByEntity(
      @Param("entityId") int entityId, @Param("entityType") int entityType
      // 此处可进一步拓展参数：
      // @Param("offset") int offset,
      // @Param("limit") int limit
      );

  // 查 2：查询某个实体下的评论总数（即符合条件的 id的个数）：
  // （如查询某一个问题的回答数量, 由于 Question表中有comment_count字段，所以需要将此函数的数据同步到 Question表中）
  @Select({
    "select count(id) from ",
    TABLE_NAME,
    " where entity_id=#{entityId} and entity_type = #{entityType}"
  })
  int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);

  // 删：删除某条评论（并不真正删除，只是将 status置为1）：
  // PS：命名时注意：DAO层只关心需要进行的数据库操作，并不关心业务逻辑，
  // 所以应根据具体操作命名为 updateStatus而不是 CommentService.java中的 deleteComment；
  @Update({"update comment set status=#{status} where id=#{id}"})
  int updateStatus(@Param("id") int id, @Param("status") int status);
}
