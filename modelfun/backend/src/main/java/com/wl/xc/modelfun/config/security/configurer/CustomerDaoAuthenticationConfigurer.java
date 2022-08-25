package com.wl.xc.modelfun.config.security.configurer;

import com.wl.xc.modelfun.config.security.component.CustomerAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.ProviderManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.userdetails.DaoAuthenticationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @version 1.0
 * @date 2022/3/31 16:37
 */
public class CustomerDaoAuthenticationConfigurer<B extends ProviderManagerBuilder<B>, U extends UserDetailsService>
    extends DaoAuthenticationConfigurer<B, U> {

  private CustomerAuthenticationProvider provider = new CustomerAuthenticationProvider();

  /**
   * Creates a new instance
   *
   * @param userDetailsService the user details service to use
   */
  public CustomerDaoAuthenticationConfigurer(U userDetailsService) {
    super(userDetailsService);
    this.provider.setUserDetailsService(userDetailsService);
    if (userDetailsService instanceof UserDetailsPasswordService) {
      this.provider.setUserDetailsPasswordService((UserDetailsPasswordService) userDetailsService);
    }
  }

  @Override
  public DaoAuthenticationConfigurer<B, U> withObjectPostProcessor(ObjectPostProcessor<?> objectPostProcessor) {
    addObjectPostProcessor(objectPostProcessor);
    return this;
  }

  @Override
  public DaoAuthenticationConfigurer<B, U> passwordEncoder(PasswordEncoder passwordEncoder) {
    this.provider.setPasswordEncoder(passwordEncoder);
    return this;
  }

  @Override
  public DaoAuthenticationConfigurer<B, U> userDetailsPasswordManager(UserDetailsPasswordService passwordManager) {
    this.provider.setUserDetailsPasswordService(passwordManager);
    return this;
  }

  @Override
  public void configure(B builder) throws Exception {
    this.provider = postProcess(this.provider);
    builder.authenticationProvider(this.provider);
  }
}
