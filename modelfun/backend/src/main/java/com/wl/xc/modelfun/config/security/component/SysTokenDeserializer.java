package com.wl.xc.modelfun.config.security.component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import java.io.IOException;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.11.2 14:17
 */
public class SysTokenDeserializer extends JsonDeserializer<SysAuthenticationToken> {

  @Override
  public SysAuthenticationToken deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    JsonNode root = p.getCodec().readTree(p);
    boolean authenticated = root.get("authenticated").asBoolean(false);
    if (!authenticated) {
      return new SysAuthenticationToken(null, null);
    }
    JsonNode details = root.get("details");
    WebRequestAuthenticationDetails webDetails = null;
    if (!details.isNull() && !details.isEmpty()) {
      webDetails =
          p.getCodec()
              .readValue(details.traverse(p.getCodec()), WebRequestAuthenticationDetails.class);
    }
    JsonNode principalNode = root.get("principal");
    SysUserDetail principal = null;
    if (!principalNode.isNull() && !principalNode.isEmpty()) {
      principal = p.getCodec().readValue(principalNode.traverse(p.getCodec()), SysUserDetail.class);
    }
    if (principal == null) {
      return new SysAuthenticationToken(null, null);
    }
    JsonNode userNode = root.get("sysUser");
    SysUserPO sysUser = null;
    if (!userNode.isNull() && !userNode.isEmpty()) {
      sysUser = p.getCodec().readValue(userNode.traverse(p.getCodec()), SysUserPO.class);
    }
    SysAuthenticationToken authenticationToken =
        new SysAuthenticationToken(principal, null, principal.getAuthorities());
    authenticationToken.setDetails(webDetails);
    authenticationToken.setSysUser(sysUser);
    return authenticationToken;
  }
}
