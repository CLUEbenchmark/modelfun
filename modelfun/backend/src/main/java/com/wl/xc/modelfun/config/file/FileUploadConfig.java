package com.wl.xc.modelfun.config.file;

import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @version 1.0
 * @date 2022/3/28 22:24
 */
//@Configuration
public class FileUploadConfig {

  @Bean
  FileUploadProgressListener fileUploadProgressListener(StringRedisTemplate stringRedisTemplate) {
    FileUploadProgressListener listener = new FileUploadProgressListener();
    listener.setStringRedisTemplate(stringRedisTemplate);
    return listener;
  }

  @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
  public MultipartResolver multipartResolver(FileUploadProgressListener fileUploadProgressListener) {
    CustomMultipartResolver resolver = new CustomMultipartResolver(fileUploadProgressListener);
    resolver.setMaxUploadSize(1024 * 1024 * 1024);
    return resolver;
  }

  static class CustomMultipartResolver extends CommonsMultipartResolver {

    private final FileUploadProgressListener listener;

    public CustomMultipartResolver(FileUploadProgressListener listener) {
      this.listener = listener;
    }

    @Override
    protected MultipartParsingResult parseRequest(HttpServletRequest request)
        throws MultipartException {
      String encoding = determineEncoding(request);
      FileUpload fileUpload = prepareFileUpload(encoding);
      Map<String, String> queryParams = parseQueryString(request.getQueryString());
      String taskId = queryParams.get("taskId");
      String projectId = queryParams.get("projectId");
      fileUpload.setProgressListener(listener);
      try {
        if (taskId != null && projectId != null) {
          listener.setCurrentTaskId(RedisKeyMethods.getFileTaskKey(0L, projectId));
        }
        List<FileItem> fileItems = ((ServletFileUpload) fileUpload).parseRequest(request);
        return parseFileItems(fileItems, encoding);
      } catch (FileUploadBase.SizeLimitExceededException ex) {
        throw new MaxUploadSizeExceededException(fileUpload.getSizeMax(), ex);
      } catch (FileUploadBase.FileSizeLimitExceededException ex) {
        throw new MaxUploadSizeExceededException(fileUpload.getFileSizeMax(), ex);
      } catch (FileUploadException ex) {
        throw new MultipartException("Failed to parse multipart servlet request", ex);
      } finally {
        listener.removeCurrentTaskId();
      }
    }

    /**
     * 解析请求参数
     *
     * @param queryString 请求参数
     * @return 解析结果
     */
    private Map<String, String> parseQueryString(String queryString) {
      String[] split = queryString.split("&");
      Map<String, String> result = new HashMap<>();
      for (String s : split) {
        String[] strings = s.split("=");
        result.put(strings[0], strings[1]);
      }
      return result;
    }
  }
}
