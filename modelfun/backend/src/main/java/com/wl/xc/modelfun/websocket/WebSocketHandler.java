package com.wl.xc.modelfun.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.config.properties.WebsocketProperties;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/5/12 16:27
 */
@ServerEndpoint(value = "/websocket/con", configurator = SpringConfigurator.class)
@Component
public class WebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

  /**
   * 连接集合，该集合是一个UID（JTI，一个用户连接的唯一标识）对应的session,映射关系为：uid -> session
   */
  private static final Map<String, Session> uidSessionMap = new ConcurrentHashMap<>();

  /**
   * session和对应用户明细的集合，映射关系为：sessionId -> payloadDTO
   */
  private static final Map<String, PayloadDTO> sessionPayloadMap = new ConcurrentHashMap<>();
  private static final Map<String, IdleTask> sessionTaskMap = new ConcurrentHashMap<>();

  private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5,
      new WorkThreadFactory("ws-heartbeat"));

  private WebsocketProperties websocketProperties;

  /**
   * 不规范的写法，但是能用就行
   */
  private static ObjectMapper mapper;

  public WebSocketHandler(ObjectMapper objectMapper) {
    executor.setKeepAliveTime(60, TimeUnit.SECONDS);
    executor.allowCoreThreadTimeOut(true);
    mapper = objectMapper;
  }

  @OnOpen
  public void onOpen(Session session) {
    Authentication userPrincipal = (Authentication) session.getUserPrincipal();
    PayloadDTO payloadDTO = (PayloadDTO) userPrincipal.getDetails();
    PayloadDTO payloadCopy = new PayloadDTO();
    payloadCopy.setJti(payloadDTO.getJti());
    payloadCopy.setUserId(payloadDTO.getUserId());
    uidSessionMap.put(payloadCopy.getJti(), session);
    sessionPayloadMap.put(session.getId(), payloadCopy);
    log.info("[WebSocketHandler.onOpen] sessionId={}, UID={} 建立ws连接", session.getId(), payloadCopy.getJti());
    // 建立连接后，开始监听心跳包
    IdleTask task = new IdleTask(
        websocketProperties.getMaxIdleCount(),
        websocketProperties.getHeartBeatTime().toNanos(), session);
    ScheduledFuture<?> schedule = executor.schedule(task,
        websocketProperties.getHeartBeatTime().toSeconds(), TimeUnit.SECONDS);
    task.setScheduledFuture(schedule);
    sessionTaskMap.put(session.getId(), task);
  }

  @OnClose
  public void onClose(Session session) {
    doCloseSession(session);
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    if (message.equalsIgnoreCase("ping")) {
      String sessionId = session.getId();
      IdleTask idleTask = sessionTaskMap.get(sessionId);
      idleTask.ping();
    }
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    log.error("[WebSocketHandler.onError]", throwable);
    if (session.isOpen()) {
      try {
        session.close();
      } catch (IOException e) {
        log.error("[WebSocketHandler.onError] 关闭出错", e);
      }
    } else {
      doCloseSession(session);
    }
  }

  private static void doCloseSession(Session session) {
    String sessionId = session.getId();
    // 清除session对应的各种映射
    PayloadDTO payloadDTO = sessionPayloadMap.remove(sessionId);
    if (payloadDTO != null) {
      uidSessionMap.remove(payloadDTO.getJti());
      log.info("[WebSocketHandler.doCloseSession] sessionId={}, UID={} 断开ws连接", sessionId, payloadDTO.getJti());
    }
    // 取消心跳任务
    IdleTask idleTask = sessionTaskMap.remove(sessionId);
    if (idleTask != null) {
      idleTask.stop();
    }
  }

  public static void sendByUid(String Uid, WebsocketDTO msg) {
    if (Uid == null) {
      log.warn("[WebSocketHandler.sendByUid] uid为空");
      return;
    }
    Session session = uidSessionMap.get(Uid);
    if (session != null) {
      String msgString;
      try {
        msgString = mapper.writeValueAsString(msg);
        send(session, msgString);
      } catch (JsonProcessingException e) {
        log.error("[WebSocketHandler.sendByUid] 消息发送失败，原因：{}", e.getMessage());
      }
    }
  }

  public static void sendByUserId(Integer userId, WebsocketDTO msg) {
    for (Entry<String, PayloadDTO> entry : sessionPayloadMap.entrySet()) {
      PayloadDTO payloadDTO = entry.getValue();
      if (payloadDTO.getUserId().equals(userId)) {
        Session session = uidSessionMap.get(payloadDTO.getJti());
        try {
          send(session, mapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
          log.error("[WebSocketHandler.sendByUserId] 消息发送失败，原因：{}", e.getMessage());
        }
      }
    }
  }

  public static void close(String Uid) {
    Session session = uidSessionMap.get(Uid);
    if (session != null) {
      try {
        session.close();
      } catch (IOException e) {
        log.error("[WebSocketHandler.close]", e);
      }
    }
  }

  private static void send(Session session, String msg) {
    if (session == null) {
      return;
    }
    if (!session.isOpen()) {
      // 如果session已经关闭，则从集合中删除
      doCloseSession(session);
      return;
    }
    try {
      session.getBasicRemote().sendText(msg);
    } catch (IOException e) {
      log.error("[WebSocketHandler.send]", e);
    }
  }

  private static class IdleTask implements Runnable {

    /**
     * 一个用来表示正在被ping的状态，主要是为了解决在ping的过程中，定时任务被执行，然后认为超时的情况
     */
    private volatile boolean isPing;
    private long lastPingTime = System.nanoTime();
    private int innerCount = 0;

    private final long delay;

    private final int count;

    private final Session session;
    private ScheduledFuture<?> scheduledFuture;

    public IdleTask(int count, long delay, Session session) {
      this.count = count;
      this.delay = delay;
      this.session = session;
    }

    public void run() {
      long nextDelay = delay;
      if (!isPing) {
        // 当前时间减去上次时间的时间差与心跳时间间隔的差
        nextDelay -= System.nanoTime() - lastPingTime;
      }
      if (nextDelay <= 0) {
        // 心跳超时
        if (++innerCount > count) {
          //stop
          log.info("[WebSocketHandler.IdleTask.run] 心跳超时，关闭连接 User:{}", sessionPayloadMap.get(session.getId()));
          if (session.isOpen()) {
            try {
              // 主动关闭连接
              session.close();
            } catch (IOException e) {
              log.error("[IdleTask.run]", e);
            }
          } else {
            doCloseSession(session);
          }
        } else {
          // 超时但是未到达设定的次数
          scheduledFuture = executor
              .schedule(this, delay, TimeUnit.NANOSECONDS);
        }
      } else {
        scheduledFuture = executor
            .schedule(this, nextDelay, TimeUnit.NANOSECONDS);
      }
    }

    public void ping() {
      isPing = true;
      lastPingTime = System.nanoTime();
      innerCount = 0;
      pong();
      isPing = false;
    }

    public void pong() {
      send(session, "pong");
    }

    public void stop() {
      if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
        scheduledFuture.cancel(true);
      }
    }

    void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
      this.scheduledFuture = scheduledFuture;
    }

  }

  @Autowired
  public void setWebsocketProperties(WebsocketProperties websocketProperties) {
    this.websocketProperties = websocketProperties;
  }
}
