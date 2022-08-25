package com.wl.xc.modelfun.entities.vo;

import com.wl.xc.modelfun.entities.model.NerTestDataModel.EntitiesDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/10 17:28
 */
@Data
@Schema(name = "NerTrainLabelDiffVO", description = "标签结果")
public class NerTrainLabelDiffVO {

  @Schema(name = "sentence", description = "标签结果")
  private String sentence;
  @Schema(name = "actual", description = "实际标签结果")
  private List<EntitiesDTO> actual;
  @Schema(name = "predict", description = "错误标签结果")
  private List<EntitiesDTO> predict;
}
