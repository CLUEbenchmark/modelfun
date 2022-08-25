package com.wl.xc.modelfun.config.servlet.component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 进行HttpServletRequest的包装，使得其中的body可以重复访问。在第一次调用getInputStream的时候才进行字节流的读取和保存.
 * <p>
 * 如果不这样做会导致后续request.getParameter读取为空。
 *
 * @version 1.0
 * @author: Fan
 * @date 2020.8.28 15:22
 */
public class RepeatAbleRequestWrapper extends HttpServletRequestWrapper {

  private ByteArrayInputStream inputStream;

  private static final int BUFFER_SIZE = 4096;

  private byte[] requestBody;

  private boolean isRead = false;

  private final HttpServletRequest request;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @param request 请求
   * @throws IllegalArgumentException if the request is null
   */
  public RepeatAbleRequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    this.request = request;
    //copyBody(request.getInputStream());
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (!isRead) {
      copyBody(request.getInputStream());
      isRead = true;
    }
    return new RepeatAbleInputStream(requestBody);
  }


  private int copyBody(InputStream in) throws IOException {
    if (in == null) {
      requestBody = new byte[0];
      return 0;
    }
    if (in.markSupported()) {
      in.reset();
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE);
    int byteCount = 0;
    byte[] buffer = new byte[BUFFER_SIZE];
    int len = 0;
    while ((len = in.read(buffer)) != -1) {
      outputStream.write(buffer, 0, len);
      byteCount += len;
    }
    outputStream.flush();
    requestBody = outputStream.toByteArray();
    return byteCount;
  }

  /**
   * 模仿spring的DefaultServerRequestBuilder中的BodyInputStream
   */
  private static class RepeatAbleInputStream extends ServletInputStream {

    private final InputStream delegate;

    protected RepeatAbleInputStream(byte[] buffer) {
      delegate = new ByteArrayInputStream(buffer);
    }

    @Override
    public boolean isFinished() {
      return false;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
      return this.delegate.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      return this.delegate.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
      return this.delegate.read(b);
    }

    @Override
    public long skip(long n) throws IOException {
      return this.delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
      return this.delegate.available();
    }

    @Override
    public void close() throws IOException {
      this.delegate.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
      this.delegate.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
      this.delegate.reset();
    }

    @Override
    public boolean markSupported() {
      return this.delegate.markSupported();
    }
  }


}
