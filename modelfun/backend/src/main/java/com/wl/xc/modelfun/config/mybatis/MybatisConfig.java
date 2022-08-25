package com.wl.xc.modelfun.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.wl.xc.modelfun.config.properties.CustomerDatasourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @version 1.0
 * @date 2020.10.12 12:50
 */
@Configuration
@MapperScan(basePackages = {"com.wl.xc.modelfun.mapper"})
@EnableTransactionManagement
public class MybatisConfig {

  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
  }

  @Bean
  public DataSource dataSource(CustomerDatasourceProperties customerDatasourceProperties,
      DataSourceProperties dataSourceProperties) {
    HikariConfig config = new HikariConfig();
    String jdbcUrl = customerDatasourceProperties.getUrl();
    if (jdbcUrl == null) {
      jdbcUrl = dataSourceProperties.getUrl();
    } else {
      jdbcUrl = reCheckUrl(jdbcUrl);
    }
    config.setJdbcUrl(jdbcUrl);
    config.setUsername(customerDatasourceProperties.getUsername() != null ? customerDatasourceProperties.getUsername()
        : dataSourceProperties.getUsername());
    config.setPassword(customerDatasourceProperties.getPassword() != null ? customerDatasourceProperties.getPassword()
        : dataSourceProperties.getPassword());
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    return new HikariDataSource(config);
  }

  private String reCheckUrl(String jdbcUrl) {
    if (jdbcUrl.isBlank()) {
      throw new IllegalArgumentException("jdbcUrl is blank");
    }
    if (!jdbcUrl.startsWith("jdbc:mysql://")) {
      throw new IllegalArgumentException("jdbcUrl is not start with jdbc:mysql://");
    }
    StringBuilder url = new StringBuilder(jdbcUrl);
    HashMap<String, String> properties = new HashMap<>();
    if (jdbcUrl.contains("?")) {
      url = new StringBuilder(jdbcUrl.substring(0, jdbcUrl.indexOf("?")));
      String propStr = jdbcUrl.substring(jdbcUrl.indexOf("?") + 1);
      if (propStr.contains("&")) {
        String[] props = propStr.split("&");
        for (String p : props) {
          String[] split = p.split("=");
          if (split.length == 2) {
            properties.put(split[0], split[1]);
          }
        }
      } else {
        String[] split = propStr.split("=");
        if (split.length == 2) {
          properties.put(split[0], split[1]);
        }
      }
    }
    // useUnicode默认为true
    properties.putIfAbsent("useUnicode", "true");
    // 如果没有设置characterEncoding，则默认使用UTF-8
    properties.putIfAbsent("characterEncoding", "UTF-8");
    properties.put("allowPublicKeyRetrieval", "true");
    properties.put("rewriteBatchedStatements", "true");
    properties.putIfAbsent("useSSL", "false");
    properties.putIfAbsent("serverTimezone", "GMT%2B8");
    url.append("?");
    for (Entry<String, String> entry : properties.entrySet()) {
      url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
    }
    url = new StringBuilder(url.substring(0, url.length() - 1));
    return url.toString();
  }

  //@Bean
  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDataSource(dataSource);
    factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
        .getResources("classpath*:mapper/*.xml"));
    return factoryBean.getObject();
  }


}
