package com.wl.xc.modelfun.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.config.properties.SecurityProperties;
import com.wl.xc.modelfun.config.security.configurer.CustomerConfigurer;
import com.wl.xc.modelfun.config.security.configurer.CustomerDaoAuthenticationConfigurer;
import com.wl.xc.modelfun.config.security.handler.JsonAuthenticationEntryPoint;
import com.wl.xc.modelfun.config.security.handler.MfLogOutHandler;
import com.wl.xc.modelfun.config.security.handler.MfLogoutSuccessHandler;
import com.wl.xc.modelfun.config.security.handler.RedisSecurityContextRepository;
import com.wl.xc.modelfun.config.websocket.WebsocketFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @version 1.0
 * @date 2020.10.18 20:21
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final SecurityProperties securityProperties;

  @Autowired
  private UserConfig userConfig;

  @Autowired
  private RedisSecurityContextRepository repository;

  @Autowired
  private ObjectMapper objectMapper;

  public SecurityConfig(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public MfLogOutHandler mfLogOutHandler() {
    return new MfLogOutHandler();
  }

  @Bean
  public MfLogoutSuccessHandler mfLogoutSuccessHandler() {
    return new MfLogoutSuccessHandler();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("*");
    configuration.setAllowedMethods(List.of("POST", "GET", "OPTIONS", "DELETE"));
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.apply(new CustomerDaoAuthenticationConfigurer<>(userConfig))
        .passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    List<String> ignoreUrls = securityProperties.getIgnoreUrls();
    String[] ignoreUrlArray = ignoreUrls.toArray(new String[0]);
    http.csrf().disable()
        .cors().configurationSource(corsConfigurationSource())
        .and()
        .securityContext().securityContextRepository(repository)
        .and()
        // .formLogin().loginProcessingUrl("/login").loginPage("/loginPage").permitAll().and()
        .authorizeRequests()
        // 自定义accessDecisionManager
        // .accessDecisionManager()
        .antMatchers(ignoreUrlArray)
        .permitAll()
        .anyRequest().authenticated()
        .and()
        .logout().addLogoutHandler(mfLogOutHandler()).logoutUrl("/doLogout")
        .logoutSuccessHandler(mfLogoutSuccessHandler())
        .permitAll()
        .and()
        .apply(new CustomerConfigurer<>(objectMapper))
        .and()
        .exceptionHandling().authenticationEntryPoint(new JsonAuthenticationEntryPoint())
        .and()
        .addFilterBefore(websocketFilter(), SecurityContextPersistenceFilter.class)
    /*// 添加自定义的session管理器。配置登录成功之后的session逻辑
        .sessionManagement().sessionAuthenticationStrategy(new SessionAuthenticationStrategy() {
      @Override
      public void onAuthentication(Authentication authentication, HttpServletRequest request,
          HttpServletResponse response) throws SessionAuthenticationException {
      }
    })*/;
  }

  @Bean
  WebsocketFilter websocketFilter() {
    return new WebsocketFilter();
  }
}
