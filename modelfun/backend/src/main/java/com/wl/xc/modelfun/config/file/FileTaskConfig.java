package com.wl.xc.modelfun.config.file;

import com.wl.xc.modelfun.tasks.file.FileTaskHandler;
import com.wl.xc.modelfun.tasks.file.FileTaskHandlerFactory;
import com.wl.xc.modelfun.tasks.file.handlers.DatasetParseHandler;
import com.wl.xc.modelfun.tasks.file.handlers.ExpertParseHandler;
import com.wl.xc.modelfun.tasks.file.handlers.ExpertParseHandler.ExpertParseInternal;
import com.wl.xc.modelfun.tasks.file.handlers.NerParseHandler;
import com.wl.xc.modelfun.tasks.file.handlers.NerParseHandler.NerInternal;
import com.wl.xc.modelfun.tasks.file.handlers.text.DatasetParseInternal;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:22
 */
@Configuration
public class FileTaskConfig {

  @Bean
  public ExpertParseHandler expertParseHandler() {
    return new ExpertParseHandler();
  }

  @Bean
  public ExpertParseInternal expertParseInternal(ExpertParseHandler expertParseHandler) {
    return expertParseHandler.new ExpertParseInternal();
  }

  @Bean
  public NerParseHandler nerParseHandler() {
    return new NerParseHandler();
  }

  @Bean
  public NerInternal nerInternal(NerParseHandler nerParseHandler) {
    return nerParseHandler.new NerInternal();
  }

  @Bean
  public DatasetParseHandler datasetParseHandler() {
    return new DatasetParseHandler();
  }

  @Bean
  public DatasetParseInternal datasetParseInternal() {
    return new DatasetParseInternal();
  }

  @Bean
  FileTaskHandlerFactory fileTaskHandlerFactory(List<FileTaskHandler> fileTaskHandlers) {
    FileTaskHandlerFactory handlerFactory = new FileTaskHandlerFactory();
    handlerFactory.addHandlers(fileTaskHandlers);
    return handlerFactory;
  }

}
