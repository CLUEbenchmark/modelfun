package com.wl.xc.modelfun.entities.req;

import com.wl.xc.modelfun.entities.model.RegexRule;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 模式匹配规则测试请求
 *
 * @version 1.0
 * @date 2022/5/31 17:22
 */
@Data
public class RegexTestReq {

  @NotBlank(message = "测试文本不能为空")
  private String text;

  @NotBlank(message = "标签描述不能为空")
  private String labelDesc;

  @NotEmpty(message = "规则列表不能为空")
  private List<List<RegexRule>> rules;

}
