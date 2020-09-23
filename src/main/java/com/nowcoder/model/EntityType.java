package com.nowcoder.model;

// 视频 6：评论中心功能代码：
// 设置 Comment表中的实体类别：
// 实体类别有两种：一条评论 既可以针对一个问题（作为一条答案） 也可以针对另一条评论（作为评论的回复）；

// 注意：在数据库中并没有建立对应的表；

public class EntityType {
  public static int ENTITY_QUESTION = 1;
  public static int ENTITY_COMMENT = 2;
}
