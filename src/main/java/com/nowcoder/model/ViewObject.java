package com.nowcoder.model;

// 视频3代码：使用 ViewObject：
// ViewObject是一个处于model对象和 velocity之间的对象；
// ViewObject:方便传递任何信息到Velocity；

import java.util.HashMap;
import java.util.Map;

public class ViewObject {
  private Map<String, Object> objs = new HashMap<String, Object>();

  public void set(String key, Object value) {
    objs.put(key, value);
  }

  public Object get(String key) {
    return objs.get(key);
  }
}
