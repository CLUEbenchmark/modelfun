package com.wl.xc.modelfun.config.redis;

import com.wl.xc.modelfun.config.properties.CustomerRedisProperties;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.util.ReflectionUtils;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.10.26 18:24
 */
@Configuration(proxyBeanMethods = false)
public class RedisConfig {

  private static final String REDIS_PROTOCOL_PREFIX = "redis://";
  private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

  @Autowired
  private RedisProperties redisProperties;

  /**
   * spring创建redis缓存的时候用到的redis配置，如果没有，则使用defaultCacheConfig.
   * <p>
   * 详细请看 @see {@link RedisCacheConfiguration}. 这里主要配置了：
   * <p>
   * 1. 配置了value的序列化方法，默认是用JdkSerializationRedisSerializer.
   * <p>
   * 2. 配置了缓存失效时间，30天.
   *
   * @return RedisCacheConfiguration
   */
  //@Bean
  public RedisCacheConfiguration redisCacheConfiguration() {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
    config = config.entryTtl(Duration.ofDays(30));
    config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
        new GenericJackson2JsonRedisSerializer()));
    return config;
  }

  //@Bean
  public UserCache userCache(RedisCacheManager cacheManager) {
    Cache cache = cacheManager.getCache("user");
    if (cache == null) {
      throw new RuntimeException("cache is null");
    }
    return new SpringCacheBasedUserCache(cache);
  }

  @Bean(destroyMethod = "shutdown")
  public RedissonClient redisson(CustomerRedisProperties customerRedisProperties) throws IOException {
    Config config = null;
    Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
    Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
    int timeout;
    if (null == timeoutValue) {
      timeout = 10000;
    } else if (!(timeoutValue instanceof Integer)) {
      Method millisMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
      timeout = ((Long) ReflectionUtils.invokeMethod(millisMethod, timeoutValue)).intValue();
    } else {
      timeout = (Integer) timeoutValue;
    }
    config = new Config();
    String prefix = REDIS_PROTOCOL_PREFIX;
    Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
    if (method != null && (Boolean) ReflectionUtils.invokeMethod(method, redisProperties)) {
      prefix = REDISS_PROTOCOL_PREFIX;
    }
    String host =
        customerRedisProperties.getHost() != null ? customerRedisProperties.getHost() : redisProperties.getHost();
    int port =
        customerRedisProperties.getPort() != null ? customerRedisProperties.getPort() : redisProperties.getPort();
    String password = customerRedisProperties.getPassword() != null ? customerRedisProperties.getPassword()
        : redisProperties.getPassword();
    int database = customerRedisProperties.getDatabase() != null ? customerRedisProperties.getDatabase()
        : redisProperties.getDatabase();
    config.useSingleServer()
        .setAddress(prefix + host + ":" + port)
        .setConnectTimeout(timeout)
        .setDatabase(database)
        .setPassword(password);
    config.setCodec(StringCodec.INSTANCE);
    return Redisson.create(config);
  }

  @Bean(name = "redissonCustomizer")
  public RedissonAutoConfigurationCustomizer customizer() {
    return configuration -> configuration.setCodec(StringCodec.INSTANCE);
  }

}
