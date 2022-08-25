package com.wl.xc.modelfun.commons;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.entities.dto.lfs.BuiltinRuleDTO;
import com.wl.xc.modelfun.entities.dto.lfs.LabelRuleDTO;
import com.wl.xc.modelfun.entities.dto.lfs.RegexRuleDTO;
import java.io.IOException;

/**
 * @version 1.0
 * @date 2022/6/2 14:22
 */
public class RuleChildTypeIdResolver extends TypeIdResolverBase {

  private JavaType superType;

  @Override
  public void init(JavaType bt) {
    this.superType = bt;
  }

  @Override
  public String idFromValue(Object value) {
    return null;
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return null;
  }

  @Override
  public Id getMechanism() {
    return Id.CUSTOM;
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    if (id == null || id.trim().isEmpty()) {
      return context.constructType(EmptyNode.class);
    }
    Class<?> subType;
    switch (id) {
      case "1":
        subType = RegexRuleDTO.class;
        break;
      case "6":
        subType = BuiltinRuleDTO.class;
        break;
      default:
        throw new BusinessIOException("未知的规则类型");
    }
    return context.constructSpecializedType(superType, subType);
  }

  @Override
  public String getDescForKnownTypeIds() {
    return "cloudPark, other";
  }


  public static class EmptyNode extends LabelRuleDTO<Object> {

  }
}
