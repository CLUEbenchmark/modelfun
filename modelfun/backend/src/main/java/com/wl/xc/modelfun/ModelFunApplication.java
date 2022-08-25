package com.wl.xc.modelfun;

import java.util.Collections;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @version 1.0
 * @date 2022/4/11 13:21
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "com.wl.xc.modelfun.config.servlet.filter")
public class ModelFunApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication();
    application.addPrimarySources(Collections.singleton(ModelFunApplication.class));
    application.setBannerMode(Mode.LOG);
    ConfigurableApplicationContext context = application.run(args);
  }
}
