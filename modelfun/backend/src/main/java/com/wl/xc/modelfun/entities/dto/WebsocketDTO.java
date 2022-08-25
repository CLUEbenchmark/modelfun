package com.wl.xc.modelfun.entities.dto;

import com.wl.xc.modelfun.commons.enums.WsEventType;
import lombok.Data;

/**
 * 用于websocket传输的数据
 *
 * @version 1.0
 * @date 2022/5/13 13:19
 */
@Data
public class WebsocketDTO {

  private WsEventType event;

  private WebsocketDataDTO data;

}
