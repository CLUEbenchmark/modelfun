package com.wl.xc.modelfun.tasks.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @version 1.0
 * @date 2022.4.16 15:25
 */
public class RuleTaskConfig {

  private Map<String, Object> params = new HashMap<>(8);

  /**
   * 根据参数的key获取对应的值
   *
   * @param key 参数的key
   * @return Optional对象，返回的参数可能为空
   */
  public Optional<Object> get(String key) {
    return Optional.ofNullable(params.get(key));
  }

  /**
   * 把键值对传入参数对象中
   *
   * @param key   key
   * @param value value
   */
  public void set(String key, Object value) {
    params.put(key, value);
  }

  /**
   * 根据key和需要返回的类型，获取对应的值，对应的值可能不存在，当不存在时，返回Optional#empty()
   *
   * @param key  key
   * @param type type
   * @param <T>  type
   * @return 返回对应的值，当不存在时，返回Optional#empty()
   */
  public <T> Optional<T> getByType(String key, Class<T> type) {
    return Optional.ofNullable(params.get(key))
        .map(
            v -> {
              if (type.isInstance(v)) {
                return type.cast(v);
              } else {
                return null;
              }
            });
  }

  public Map<String, Object> getParams() {
    return this.params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }
}
