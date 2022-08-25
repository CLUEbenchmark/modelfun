SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `mf_dataset_detail`
(
    `id`              BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `task_id`         BIGINT                                                 NOT NULL COMMENT '记录ID',
    `dataset_id`      INT                                                    NOT NULL COMMENT '数据集ID',
    `file_type`       INT                                                    NOT NULL COMMENT '文件类型。1：测试集，2：训练集，3：标签集',
    `file_address`    VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '文件地址',
    `update_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_task_type` (`task_id` ASC, `file_type` ASC) USING BTREE COMMENT '唯一索引，每个任务每种类型的文件只能有一个'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_dataset_info`
(
    `id`              INT                                                    NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `name`            VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '集合名称',
    `task_id`         BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_address` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '集合文件地址',
    `dataset_desc`    VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '集合描述',
    `create_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_poeple`   VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '创建人',
    `update_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_people`   VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '更新人',
    `deleted`         TINYINT                                                NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_id` (`task_id` ASC) USING BTREE COMMENT '任务ID索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '数据集信息表，对应任务下的数据集信息'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_gpt_cache`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `task_id`     BIGINT NOT NULL COMMENT '任务ID',
    `data_type`   INT    NOT NULL COMMENT '数据类型：1-展示的测试集，2-未标注数据集',
    `sentence_id` BIGINT NOT NULL COMMENT '数据ID',
    `rule_id`     BIGINT NOT NULL COMMENT '规则ID',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `ik_task_sen` (`task_id` ASC, `data_type` ASC, `sentence_id` ASC) USING BTREE COMMENT '任务下，对应数据id是否被gpt3模型使用的记录'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_integrate_label_result`
(
    `id`          BIGINT                                                 NOT NULL AUTO_INCREMENT,
    `task_id`     BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_id`  BIGINT                                                 NULL DEFAULT NULL COMMENT '数据集ID',
    `sentence_id` BIGINT                                                 NOT NULL COMMENT '语料ID',
    `sentence`    TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NOT NULL COMMENT '语料内容',
    `label_id`    INT                                                    NOT NULL COMMENT '标签ID',
    `label_des`   VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标签描述',
    `op_status`   INT                                                    NULL DEFAULT NULL COMMENT '操作状态，预留字段',
    `used`        TINYINT                                                NULL DEFAULT NULL COMMENT '是否使用，预留状态',
    `data_type`   INT                                                    NOT NULL COMMENT '数据类型，1：高置信数据；2：待审核数据',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_sentence` (`task_id` ASC, `data_type` ASC, `sentence_id` ASC) USING BTREE,
    INDEX `ik_sentence` (`sentence_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '规则集成标签结果表'
  ROW_FORMAT = DYNAMIC;


CREATE TABLE IF NOT EXISTS `mf_integration_records`
(
    `id`                    BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `task_id`               BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_id`            INT                                                    NOT NULL COMMENT '数据集ID',
    `result_file_address`   VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '自动标注结果文件地址',
    `labeled`               INT                                                    NOT NULL DEFAULT 0 COMMENT '是否被标注 0: 否 1：是',
    `vote_model_address`    VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '集成之后的模型文件地址',
    `mapping_model_address` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '集成之后，标签映射的地址',
    `integrate_status`      INT                                                    NOT NULL DEFAULT 0 COMMENT '集成状态。0：集成中，1：集成成功，3：集成失败',
    `test_accuracy`         VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '测试集准确率',
    `test_recall`           VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '测试集召回率',
    `test_f1_score`         VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '测试集f1_score',
    `train_label_count`     INT                                                    NULL     DEFAULT NULL COMMENT '训练集标签类别数',
    `train_sentence_count`  BIGINT                                                 NULL     DEFAULT NULL COMMENT '训练集标注语料量',
    `unlabel_coverage`      VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '未标注数据集覆盖率',
    `create_datetime`       DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_datetime`       DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `time_cost`             INT                                                    NULL     DEFAULT NULL COMMENT '耗时时间，单位秒',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_time` (`task_id` ASC, `create_datetime` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '规则集成记录表，该表记录的是规则训练的流水记录'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_integration_result`
(
    `id`              INT                                                    NOT NULL AUTO_INCREMENT COMMENT '集成结果记录ID',
    `task_id`         BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_id`      INT                                                    NULL     DEFAULT NULL COMMENT '数据集ID',
    `integration_id`  BIGINT                                                 NULL     DEFAULT NULL COMMENT '数据集成记录ID',
    `rule_id`         BIGINT                                                 NOT NULL COMMENT '规则ID',
    `label_id`        INT                                                    NULL     DEFAULT NULL COMMENT '标签ID',
    `label_des`       VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '标签内容',
    `accuracy`        VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '准确率',
    `coverage`        VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '覆盖率',
    `repeat`          VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '重复率',
    `conflict`        VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '冲突率',
    `create_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `index_task_id` (`task_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '集成结果表：Integration_result'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_label_info`
(
    `id`                  BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '标签记录ID',
    `task_id`             BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_id`          INT                                                    NOT NULL COMMENT '数据集ID',
    `label_id`            INT                                                    NOT NULL COMMENT '标签ID',
    `label_desc`          TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NOT NULL COMMENT '标签描述',
    `high_frequency_word` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL COMMENT '高频词统计',
    `description`         TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL COMMENT '标签说明',
    `example`             TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL COMMENT '示例说明',
    `update_datetime`     DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_people`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `index_task_label` (`task_id` ASC, `label_id` ASC) USING BTREE COMMENT '任务ID索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '标签集表，对应数据集中的标签文件内容'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_ner_auto_label_map`
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT,
    `task_id`      BIGINT NOT NULL COMMENT '任务ID',
    `sentence_id`  BIGINT NOT NULL COMMENT '语料ID',
    `label_id`     INT    NOT NULL COMMENT '标签ID',
    `start_offset` INT    NOT NULL COMMENT '起始位置',
    `end_offset`   INT    NOT NULL COMMENT '结束位置',
    `data_id`      BIGINT NULL DEFAULT NULL COMMENT '实体ID',
    `data_type`    INT    NOT NULL COMMENT '数据类型：标注结果、错误数据',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_sentence` (`task_id` ASC, `data_type` ASC, `sentence_id` ASC) USING BTREE,
    INDEX `ik_task_label` (`task_id` ASC, `data_type` ASC, `label_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;


CREATE TABLE IF NOT EXISTS `mf_ner_auto_label_result`
(
    `id`          BIGINT                                             NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `task_id`     BIGINT                                             NOT NULL COMMENT '任务ID',
    `sentence_id` BIGINT                                             NOT NULL COMMENT '语料ID',
    `sentence`    LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '语料',
    `relations`   LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT 'relations',
    `data_type`   INT                                                NOT NULL COMMENT '数据类型：标注结果、错误数据',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_sentence` (`task_id` ASC, `data_type` ASC, `sentence_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_ner_auto_label_train`
(
    `id`      BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL,
    `data_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_ner_data_label`
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT,
    `task_id`      BIGINT NOT NULL COMMENT '任务ID',
    `sentence_id`  BIGINT NOT NULL COMMENT '语料ID',
    `label_id`     INT    NOT NULL COMMENT '标签ID',
    `start_offset` INT    NOT NULL COMMENT '起始位置',
    `end_offset`   INT    NOT NULL COMMENT '结束位置',
    `data_id`      BIGINT NULL DEFAULT NULL COMMENT '实体ID',
    `data_type`    INT    NOT NULL COMMENT '数据类型，测试集，训练集等',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_label` (`task_id` ASC, `label_id` ASC) USING BTREE,
    INDEX `ik_task_sentence` (`task_id` ASC, `data_type` ASC, `sentence_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_ner_test_data`
(
    `id`        BIGINT                                             NOT NULL AUTO_INCREMENT,
    `task_id`   BIGINT                                             NOT NULL COMMENT '任务ID',
    `data_id`   BIGINT                                             NOT NULL COMMENT '语料ID，根据行号生成',
    `sentence`  LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '语料',
    `relations` JSON                                               NULL COMMENT '冗余字段',
    `show_data` INT                                                NULL DEFAULT NULL COMMENT '是否展示，冗余字段',
    `data_type` INT                                                NOT NULL COMMENT '数据类型：测试集、训练集等',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `ik_task_data` (`task_id` ASC, `data_type` ASC, `data_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_ner_train_label_detail`
(
    `id`             BIGINT NOT NULL AUTO_INCREMENT,
    `train_label_id` BIGINT NOT NULL,
    `data_id`        BIGINT NOT NULL COMMENT '语料',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_ner_train_label_result`
(
    `id`              BIGINT                                                 NOT NULL AUTO_INCREMENT,
    `train_record_id` BIGINT                                                 NOT NULL,
    `label_des`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `train_precision` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL,
    `recall`          VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL,
    `samples`         INT                                                    NULL DEFAULT NULL COMMENT '样本数',
    `error_count`     INT                                                    NULL DEFAULT NULL COMMENT '预测错误数',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_rule_info`
(
    `id`                 BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `task_id`            BIGINT                                                 NULL     DEFAULT NULL COMMENT '任务ID',
    `dataset_id`         INT                                                    NULL     DEFAULT NULL COMMENT '数据集ID',
    `rule_type`          INT                                                    NOT NULL COMMENT '规则类型（1：模式匹配，2：专家知识，3：数据库，4：外部api）',
    `rule_name`          VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '规则名称',
    `label`              INT                                                    NULL     DEFAULT NULL COMMENT '标签ID',
    `label_des`          VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '标签描述',
    `accuracy`           VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '准确率',
    `coverage`           VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '测试集数据覆盖率',
    `conflict`           VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '冲突率，面向该条标注规则标记的语料，该条标注规则标注的语料与其他标注规则产生冲突的语料',
    `completed`          INT                                                    NOT NULL DEFAULT 0 COMMENT '规则是否运行完成 0:运行中，1：完成 2：失败',
    `unlabeled_coverage` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '对应未标注数据集的覆盖率',
    `overlap`            VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '重叠率，面向该条标注规则标记的语料，该条标注规则标注的结果与其他标注规则标注的结果产生重复的语料',
    `metadata`           LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL COMMENT '规则声明描述',
    `create_datetime`    DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_start_time`  DATETIME                                               NULL     DEFAULT NULL COMMENT '规则开始创建的时间',
    `create_end_time`    DATETIME                                               NULL     DEFAULT NULL COMMENT '规则创建结束的时间',
    `update_datetime`    DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_start_time`  DATETIME                                               NULL     DEFAULT NULL COMMENT '规则更新开始的时间',
    `update_end_time`    DATETIME                                               NULL     DEFAULT NULL COMMENT '规则更新结束的时间',
    `auto_generated`     TINYINT                                                NULL     DEFAULT 0 COMMENT '是否是平台自动生成的规则 true：是 false：否',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `index_task_id` (`task_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = 'modelfun标注规则表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_rule_overview`
(
    `id`                 BIGINT                                                NOT NULL AUTO_INCREMENT COMMENT '标注规则概览记录ID',
    `task_id`            BIGINT                                                NOT NULL COMMENT '任务ID',
    `accuracy`           VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '准确率',
    `conflict`           VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '冲突率',
    `coverage`           VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '覆盖率',
    `test_data_coverage` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '测试集的覆盖率',
    `create_datetime`    DATETIME                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_datetime`    DATETIME                                              NULL     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `ik_task` (`task_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '任务规则概览表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_rule_result`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `task_id`     BIGINT NOT NULL COMMENT '任务ID',
    `rule_id`     BIGINT NOT NULL COMMENT '规则ID',
    `sentence_id` BIGINT NOT NULL COMMENT '测试集中语料ID',
    `label_id`    INT    NOT NULL COMMENT '标签集中的标签ID',
    `show_data`   INT    NULL DEFAULT NULL COMMENT '是否展示0否 1是',
    `data_type`   INT    NOT NULL COMMENT '数据类型，4：验证集；5：测试集',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_sentence` (`sentence_id` ASC) USING BTREE,
    INDEX `ik_task_rule` (`task_id` ASC, `rule_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '记录每条规则下，每条语料对应的标签'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_rule_unlabeled_result`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `task_id`     BIGINT NOT NULL COMMENT '任务ID',
    `rule_id`     BIGINT NOT NULL COMMENT '规则ID',
    `sentence_id` BIGINT NOT NULL COMMENT '未标注数据集中的语料ID',
    `label_id`    INT    NOT NULL COMMENT '该规则下未标注数据集语料的标签',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task_rule` (`task_id` ASC, `rule_id` ASC) USING BTREE,
    INDEX `ik_sentence` (`sentence_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '未标注数据集经过规则运行后打上的标签'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_rule_vote_result`
(
    `task_id`     BIGINT NOT NULL COMMENT '任务ID',
    `sentence_id` BIGINT NOT NULL COMMENT '语料ID',
    `label_id`    INT    NOT NULL COMMENT '标签集中的标签ID',
    INDEX `ik_task` (`task_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '规则最终结果表，存储的是所有规则投票完成之后，最后的语料对应的标签'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_task_expert`
(
    `id`              BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '专家知识ID',
    `task_id`         BIGINT                                                 NOT NULL COMMENT '任务ID',
    `file_name`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '专家知识文件名称',
    `file_address`    VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '专家知识文件地址',
    `create_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_task` (`task_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '任务下的专家知识表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_task_info`
(
    `id`              BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id`         INT                                                    NOT NULL COMMENT '任务所属用户ID',
    `name`            VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '任务名称',
    `domain`          VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '任务领域',
    `task_type`       INT                                                    NOT NULL COMMENT '任务类型',
    `language_type`   INT                                                    NOT NULL COMMENT '语言类型',
    `keyword`         VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '关键词',
    `task_desc`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '任务描述',
    `deleted`         TINYINT                                                NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_people`   VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '创建人',
    `create_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_people`   VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '更新人',
    `update_datetime` DATETIME                                               NULL     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_userid` (`user_id` ASC) USING BTREE COMMENT '用户ID索引',
    INDEX `ik_uptime_user_en` (`user_id` ASC, `deleted` ASC, `update_datetime` DESC) USING BTREE COMMENT '时间索引'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '任务信息表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_test_data`
(
    `id`          BIGINT                                                NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `task_id`     BIGINT                                                NOT NULL COMMENT '任务ID',
    `data_set_id` INT                                                   NOT NULL COMMENT '数据集ID',
    `data_id`     BIGINT                                                NOT NULL COMMENT '数据ID',
    `label`       INT                                                   NULL DEFAULT NULL COMMENT '标签ID',
    `sentence`    TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '语料内容',
    `label_des`   TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标签内容',
    `show_data`   INT                                                   NULL DEFAULT NULL COMMENT '是否展示0：否 1：是',
    `data_type`   INT                                                   NOT NULL COMMENT '数据类型：1-测试集全集；4:-验证集；5-测试集；8：训练集',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_taskid_dataid` (`task_id` ASC, `data_type` ASC, `data_id` ASC) USING BTREE COMMENT '唯一索引，每个数据集中的数据ID唯一'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '数据集中测试集数据的明细，每条记录对应一条数据'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_train_label_sentence_info`
(
    `id`              BIGINT                                             NOT NULL AUTO_INCREMENT,
    `train_record_id` BIGINT                                             NOT NULL,
    `data_id`         BIGINT                                             NOT NULL,
    `sentence`        LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    `label_actual`    LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    `label_predict`   LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;


CREATE TABLE IF NOT EXISTS `mf_train_records`
(
    `id`              BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '模型训练操作记录ID',
    `task_id`         BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_id`      INT                                                    NULL     DEFAULT NULL COMMENT '数据集ID',
    `data_version`    VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '数据版本',
    `train_status`    INT                                                    NOT NULL COMMENT '训练结果(0:训练中，1：训练完成，-1：取消训练, 2训练失败)',
    `train_file`      VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '训练关联文件地址',
    `model_type`      INT                                                    NULL     DEFAULT NULL COMMENT '模型类型：1-LR；2-BERT',
    `create_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `label_count`     INT                                                    NULL     DEFAULT NULL COMMENT '标签数量',
    `rule_count`      INT                                                    NULL     DEFAULT NULL COMMENT '规则数量，这里是指训练时运行成功的规则的数量',
    `train_count`     INT                                                    NULL     DEFAULT NULL COMMENT '训练集数量',
    `label_array`     LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL COMMENT '标签数组',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `index_task` (`task_id` ASC) USING BTREE,
    INDEX `index_time` (`update_datetime` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '模型训练记录表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_train_result`
(
    `id`               BIGINT                                                 NOT NULL AUTO_INCREMENT COMMENT '模型训练结果记录ID',
    `task_id`          BIGINT                                                 NOT NULL COMMENT '任务ID',
    `dataset_id`       INT                                                    NULL     DEFAULT NULL COMMENT '数据集ID',
    `train_count`      INT                                                    NULL     DEFAULT NULL COMMENT '训练集数量',
    `train_record_id`  BIGINT                                                 NOT NULL COMMENT '训练记录ID',
    `rule_count`       INT                                                    NULL     DEFAULT NULL COMMENT '标注规则数量',
    `label_type_count` INT                                                    NULL     DEFAULT NULL COMMENT '标签类别数量',
    `coverage`         VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '覆盖率',
    `accuracy`         VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '准确率',
    `train_precision`  VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '精准率',
    `recall`           VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '召回率',
    `f1_score`         VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT 'f1 score',
    `module_type`      INT                                                    NULL     DEFAULT NULL COMMENT '模型类型(1：投票模型，2：BERT)',
    `file_address`     VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '文件地址',
    `create_datetime`  DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `confusion_mx`     LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL COMMENT '混淆矩阵，一个二维数组的json字符串',
    `label_array`      LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL COMMENT '标签数组',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `index_task_train` (`task_id` ASC, `train_record_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '模型训练结果表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `mf_unlabel_data`
(
    `id`          BIGINT                                                NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `task_id`     BIGINT                                                NOT NULL COMMENT '任务ID',
    `data_set_id` INT                                                   NOT NULL COMMENT '数据集ID',
    `data_id`     BIGINT                                                NOT NULL COMMENT '数据ID',
    `label`       INT                                                   NULL DEFAULT NULL COMMENT '标签ID',
    `sentence`    TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '语料内容',
    `label_des`   TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '标签内容',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_taskid_dataid` (`task_id` ASC, `data_id` ASC) USING BTREE COMMENT '唯一索引，每个数据集中的数据ID唯一'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '数据集中未标注数据的明细，每条记录对应一条数据'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `sys_dict`
(
    `id`              INT                                              NOT NULL AUTO_INCREMENT,
    `map_key`         VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NULL     DEFAULT NULL COMMENT '字典映射的key',
    `map_value`       VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NULL     DEFAULT NULL COMMENT '字典映射的value',
    `map_group`       VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NULL     DEFAULT NULL COMMENT '字典映射分组',
    `map_desc`        VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NULL     DEFAULT NULL COMMENT ' 描述',
    `map_sort`        INT                                              NOT NULL DEFAULT 1 COMMENT '排序',
    `create_datetime` DATETIME                                         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_people`   VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NULL     DEFAULT NULL COMMENT '创建人',
    `update_datetime` DATETIME                                         NULL     DEFAULT NULL COMMENT '更新时间',
    `update_people`   DATETIME                                         NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_bin COMMENT = '字典表'
  ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`              INT                                                     NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_name`       VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名称',
    `user_phone`      VARCHAR(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '用户手机号',
    `user_password`   VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户密码',
    `create_datetime` DATETIME                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_datetime` DATETIME                                                NULL     DEFAULT NULL COMMENT '修改时间',
    `update_people`   VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '更新人员',
    `remark`          VARCHAR(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '备注信息',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_user_phone` (`user_phone` ASC) USING BTREE COMMENT '手机号码，唯一'
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户表'
  ROW_FORMAT = DYNAMIC;

INSERT INTO sys_user (`user_name`, `user_phone`, `user_password`, `create_datetime`) SELECT 'admin', '13012345678', '$2a$12$CpK6flyONpY2r6HoEM535.eu7.BcwXmFEr8CbFutftV3TVzIzhyBm', '2022-03-31 11:58:59' FROM DUAL WHERE NOT EXISTS (SELECT id FROM sys_user WHERE user_phone = '13012345678');

CREATE TABLE IF NOT EXISTS `sys_user_feedback`
(
    `id`              BIGINT                                                 NOT NULL COMMENT '数据ID',
    `user_id`         INT                                                    NOT NULL COMMENT '用户ID',
    `question`        LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL,
    `appendix1`       VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '附件地址1',
    `appendix2`       VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '附件地址2',
    `appendix3`       VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '附件地址3',
    `appendix4`       VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '附件地址4',
    `question_status` INT                                                    NOT NULL DEFAULT 0 COMMENT '问题状态：1- 待处理 2- 已回复 3- 已忽略',
    `answer`          LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL COMMENT '回复',
    `answer_people`   INT                                                    NOT NULL COMMENT '回复人员ID',
    `remark`          LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin     NULL COMMENT '备注',
    `create_datetime` DATETIME                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_datetime` DATETIME                                               NULL     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `ik_user` (`user_id` ASC) USING BTREE,
    INDEX `ik_u_time` (`create_datetime` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;