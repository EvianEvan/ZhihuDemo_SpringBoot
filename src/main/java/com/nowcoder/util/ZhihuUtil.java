package com.nowcoder.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

// 视频 4-1代码：
// 使用 MD5为密码进行加密，本 MD5 函数的代码来自网络，课程中没有讲解，直接用；

// 视频 5 添加代码：
// JSON字符串生成函数：利用 fastjson库生成 JSON字符串；

public class ZhihuUtil {
  private static final Logger logger = LoggerFactory.getLogger(ZhihuUtil.class);

  // 1. MD5 加密函数：
  public static String MD5(String key) {
    char hexDigits[] = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    try {
      byte[] btInput = key.getBytes();
      // 获得MD5摘要算法的 MessageDigest 对象
      MessageDigest mdInst = MessageDigest.getInstance("MD5");
      // 使用指定的字节更新摘要
      mdInst.update(btInput);
      // 获得密文
      byte[] md = mdInst.digest();
      // 把密文转换成十六进制的字符串形式
      int j = md.length;
      char str[] = new char[j * 2];
      int k = 0;
      for (int i = 0; i < j; i++) {
        byte byte0 = md[i];
        str[k++] = hexDigits[byte0 >>> 4 & 0xf];
        str[k++] = hexDigits[byte0 & 0xf];
      }
      return new String(str);
    } catch (Exception e) {
      logger.error("生成MD5失败", e);
      return null;
    }
  }

  // 2. JSON字符串生成函数：将输入参数转换成JSON字符串；
  public static String getJSONString(int code, String msg) {
    JSONObject json = new JSONObject();
    json.put("code", code);
    json.put("msg", msg);
    return json.toJSONString();
  }

  // 重载函数：处理另一种输入：
  public static String getJSONString(int code) {
    JSONObject json = new JSONObject();
    json.put("code", code);
    return json.toJSONString();
  }

  // 为未登录用户设置ID（匿名用户）：
  public static int ANONYMOUS_USERID = 999;
}
