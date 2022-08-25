package com.wl.xc.modelfun.entities.po;

import lombok.Data;

/**
 * 语料对应的投票结果，其内部结构为：
 * <p>
 * 1:1,2,3,-1
 * <p>
 * 即，一条语料对应每个规则的投票结果，每个规则的投票结果用逗号分隔
 *
 * @version 1.0
 * @date 2022.4.16 1:56
 */
@Data
public class RuleVotePO {

  private Long sentenceId;

  private String labelVote;

}
