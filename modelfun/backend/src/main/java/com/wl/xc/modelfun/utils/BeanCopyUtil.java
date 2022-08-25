package com.wl.xc.modelfun.utils;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cglib.beans.BeanCopier;

/**
 * java bean属性的拷贝工具，原理是通过cglib在运行时动态生成类，拷贝源调用get获取属性值，目标源调用set方法传值。 set方法返回值不一样的属性不能拷贝。
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/3/30 17:31
 */
public class BeanCopyUtil {

  private static final ConcurrentHashMap<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>(16);


  public static void copy(Object src, Object target) {
    String key = genKey(src.getClass(), target.getClass());
    if (!BEAN_COPIER_CACHE.containsKey(key)) {
      synchronized (BEAN_COPIER_CACHE) {
        if (!BEAN_COPIER_CACHE.containsKey(key)) {
          BeanCopier copier = BeanCopier.create(src.getClass(), target.getClass(), false);
          BEAN_COPIER_CACHE.put(key, copier);
        }
      }
    }
    BeanCopier copier = BEAN_COPIER_CACHE.get(key);
    copier.copy(src, target, null);
  }

  private static String genKey(Class<?> srcClazz, Class<?> tgtClazz) {
    return srcClazz.getName() + "@" + tgtClazz.getName();
  }

}
