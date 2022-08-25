package com.wl.xc.modelfun.commons.validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义枚举类的验证
 *
 * @version 1.0
 * @author: Fan
 * @date 2021.1.7 11:07
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Constraint(validatedBy = EnumValidator.EnumValidatorHandle.class)
public @interface EnumValidator {

  /**
   * 要验证的枚举类的类型
   */
  Class<? extends Enum<?>> value();

  /**
   * 枚举类中获取值的方法名
   */
  String method();

  String message() default "枚举值不存在";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};


  /**
   * 处理EnumValidator注解的处理类
   */
  class EnumValidatorHandle implements ConstraintValidator<EnumValidator, Object>, Annotation {

    private static final Logger log = LoggerFactory.getLogger(EnumValidatorHandle.class);
    private String method;
    private List<Object> values;
    private static final ConcurrentHashMap<Class<? extends Enum<?>>, List<Object>> ENUM_CACHE =
        new ConcurrentHashMap<>(16);

    @Override
    public void initialize(EnumValidator enumValidator) {
      Class<? extends Enum<?>> enumClass = enumValidator.value();
      method = enumValidator.method();
      if (!ENUM_CACHE.containsKey(enumClass)) {
        synchronized (ENUM_CACHE) {
          if (!ENUM_CACHE.containsKey(enumClass)) {
            getEnumMap(enumClass);
          }
        }
      }
      values = ENUM_CACHE.get(enumClass);
    }

    private void getEnumMap(Class<? extends Enum<?>> enumClass) {
      Enum<?>[] enums = enumClass.getEnumConstants();
      ArrayList<Object> objects = new ArrayList<>();
      try {
        Method getter = enumClass.getMethod(method);
        for (Enum<?> value : enums) {
          Object result = getter.invoke(value);
          objects.add(result);
        }
      } catch (Exception e) {
        log.error("初始化{}失败，原因：{}", enumClass.getName(), e.getMessage());
      }
      // 即使异常了，也把值塞进去，因为相同的代码下次还是会异常。
      ENUM_CACHE.put(enumClass, objects);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
      if (value == null) {
        return false;
      }
      if (values == null || values.isEmpty()) {
        return false;
      }
      return values.contains(value);
    }
  }

}
