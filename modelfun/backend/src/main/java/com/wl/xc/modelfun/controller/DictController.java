package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.entities.req.DictPageReq;
import com.wl.xc.modelfun.entities.req.DictReq;
import com.wl.xc.modelfun.entities.vo.DictDetailVO;
import com.wl.xc.modelfun.entities.vo.DictKeyValueVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/12 14:27
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/dict")
public class DictController {

  private DictService dictService;


  @Operation(method = "POST", summary = "分页查询字典")
  @PostMapping("/page")
  public PageResultVo<List<DictDetailVO>> getDictDetailPage(DictPageReq req) {
    PageVO<DictDetailVO> page = dictService.getDictDetailPage(req);
    return PageResultVo.createSuccess(page);
  }

  @Operation(method = "POST", summary = "全量查询字典")
  @PostMapping("/list")
  public ResultVo<List<DictKeyValueVO>> getDict(@RequestBody DictReq req) {
    List<DictKeyValueVO> result = dictService.getDictList(req);
    return ResultVo.createSuccess(result);
  }

  @Autowired
  public void setDictService(DictService dictService) {
    this.dictService = dictService;
  }
}
