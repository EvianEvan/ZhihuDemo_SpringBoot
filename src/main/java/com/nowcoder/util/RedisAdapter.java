package com.nowcoder.util;

// 视频 7 ：Redis数据库介绍 + 利用 Redis实现赞踩功能：
//
// 1. Redis介绍：推荐 阅读《 Redis设计与实现》并同时阅读 Redis源码：
// Redis 是 key-value数据库，数据存在内存，性能卓越。
//
// 传统的关系型数据库是一张二维表；
// Redis不是表，是由一个 key作为入口，一个点进去后面是一个片；
//
//
// • Redis-KV
// • Redis-List
// • Redis-Zset
// • Redis-Hash
//
//
// 补充 1：Redis命令行里的命令(可直接与 Redis进行交互)：
// keys *
// set aaa bbb
// get aaa
//

// 补充 2：Redis 进行数据持久化硬盘备份的两种方式：RDB（Redis DataBase）和 AOF（Append Only File）：
// 必看文档：https://redis.io/topics/persistence
// 一、RDB：把当前数据生成快照保存在硬盘上。(若经常进行改变的 key较少则推荐 RDB)
// RDB（Redis DataBase）持久化是把当前 Redis 中全部数据生成快照保存在硬盘上。
// RDB 持久化可以手动触发，也可以自动触发。
// RDB 文件是一个紧凑的二进制压缩文件，是 Redis 在某个时间点的全部数据快照。
// 所以使用 RDB 恢复数据的速度远远比 AOF 的快，非常适合备份、全量复制、灾难恢复等场景。
// 缺点：属于重量级操作，频繁执行成本过高，所以无法做到实时持久化，或者秒级持久化。
// 1.如果你想保证数据的高可用性，即最大限度的避免数据丢失，那么 RDB 将不是一个很好的选择。
// 因为系统一旦在定时持久化之前出现宕机现象，此前没有来得及写入磁盘的数据都将丢失。
// 2.由于 RDB 是通过 fork 子进程来协助完成数据持久化工作的，
// 因此，如果当数据集较大时，可能会导致整个服务器停止服务几百毫秒，甚至是 1 秒钟。
//
// 二、AOF：记录每次对数据的操作到硬盘上。
// AOF（Append Only File）持久化是把每次写命令追加写入日志中，
// 当需要恢复数据时重新执行 AOF 文件中的命令就可以了。
// AOF 解决了数据持久化的实时性，也是目前主流的 Redis持久化方式。
// 对于相同数量的数据集而言，AOF 文件通常要大于 RDB 文件。RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快。
// 参考：https://www.cnblogs.com/zxs117/p/11242026.html

// 补充 3：Redis配置文件（redis\64bit\redis.conf）中的一些重要设置选项：
// # Set the number of databases. The default database is DB 0, you can select
// # a different one on a per-connection basis using SELECT <dbid> where
// # dbid is a number between 0 and 'databases'-1 （设置数据库的最大数量）
// databases 16
//
// #
// # Save the DB on disk:
// #
// #   save <seconds> <changes>
// #
// #   Will save the DB if both the given number of seconds and the given
// #   number of write operations against the DB occurred.
// #
// #   In the example below the behaviour will be to save:
// #   after 900 sec (15 min) if at least 1 key changed (如果900秒内至少有一个key被改变则进行备份（从内存同步到硬盘）)
// #   after 300 sec (5 min) if at least 10 keys changed (如果300秒内至少有十个key被改变则进行备份)
// #   after 60 sec if at least 10000 keys changed (如果60秒内至少有一万个key被改变则进行备份)
// #
// #   Note: you can disable saving at all commenting all the "save" lines.
//
// save 900 1
// save 300 10
// save 60 10000
//
// # The filename where to dump the DB （设置备份时，保存到硬盘的数据库文件名称）
// dbfilename dump.rdb
//
// # The working directory.
// #
// # The DB will be written inside this directory, with the filename specified
// # above using the 'dbfilename' configuration directive.
// #
// # Also the Append Only File will be created inside this directory.
// #
// # Note that you must specify a directory here, not a file name.（设置备份时，保存到硬盘的数据库文件路径）
// dir ./

// 补充 5：Redis针对各个语言的客户端框架（https://redis.io/clients）：
// Java 推荐 Jedis，本项目使用的就是该框架（https://github.com/xetorthio/jedis）；
//
// Jedis支持集群（互为主备）和哨兵功能；
// 使用前只需在pom.xml中添加依赖即可；
//
// Jedis的使用方法： To use it just:
// Jedis jedis = new Jedis("localhost");
// jedis.set("foo", "bar");
// String value = jedis.get("foo");

import org.springframework.beans.factory.InitializingBean;

public class RedisAdapter implements InitializingBean {

  // 自定义 print 函数：
  public static void print(int index, Object obj) {
    System.out.println(String.format("%d, %s", index, obj.toString()));
  }

  // 主函数：
  public static void main(String[] args) {
    //    print();
  }

  // InitializingBean 接口函数：
  // InitializingBean 接口为 bean 提供了初始化方法的方式，
  // 它只包括 afterPropertiesSet方法，凡是继承该接口的类，在初始化 bean的时候都会执行该方法。
  @Override
  public void afterPropertiesSet() throws Exception {}
}
