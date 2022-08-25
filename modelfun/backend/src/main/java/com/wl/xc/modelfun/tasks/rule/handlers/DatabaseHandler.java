package com.wl.xc.modelfun.tasks.rule.handlers;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.DatabaseRule;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @version 1.0
 * @date 2022/4/15 13:40
 */
@Slf4j
public class DatabaseHandler extends AbstractOutLabelHandler {

  private final DatabaseRule databaseRule;

  private final String sql;

  private Connection connection;

  private Map<String, Integer> map = new HashMap<>(5000);

  public DatabaseHandler(DatabaseRule databaseRule, Long taskId) {
    super(taskId);
    this.databaseRule = databaseRule;
    sql =
        String.format(
            "select %s,%s from %s limit 10000",
            databaseRule.getSentenceColumn(),
            databaseRule.getLabelColumn(),
            databaseRule.getTable());
  }

  @Override
  public RuleType getRuleType() {
    return RuleType.DATABASE;
  }

  @Override
  public void init() {
    super.init();
    // jdbc:mysql://localhost:3306/mofun?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&rewriteBatchedStatements=true
    String url =
        "jdbc:mysql://"
            + databaseRule.getHost()
            + ":"
            + databaseRule.getPort()
            + "/"
            + databaseRule.getDatabase()
            + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
    log.info("[DatabaseHandler.init] url:{}", url);
    try {
      connection = DriverManager.getConnection(url, databaseRule.getUser(), databaseRule.getPassword());
      log.info("[DatabaseHandler.init] 开始从数据库中加载数据，sql={}", sql);
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        String sentence = resultSet.getString(databaseRule.getSentenceColumn());
        Object object = resultSet.getObject(databaseRule.getLabelColumn());
        if (object instanceof Integer) {
          if (labelIds.contains((Integer) object)) {
            map.put(sentence, (Integer) object);
          }
        } else if (object instanceof Long) {
          if (labelIds.contains(((Long) object).intValue())) {
            map.put(sentence, ((Long) object).intValue());
          }
        } else if (object instanceof String) {
          map.put(sentence, getSystemLabelId((String) object));
        }
      }
      log.info("[DatabaseHandler.init] 从外部数据库获取数据，获取数据条数：{}", resultSet.getRow());
    } catch (Exception e) {
      throw new BusinessIllegalStateException("获取外部数据库数据失败！", e);
    } finally {
      if (connection != null) {
        try {
          connection.close();
          connection = null;
        } catch (SQLException e) {
          log.error("[DatabaseHandler.init] 连接关闭失败", e);
        }
      }
    }
  }

  @Override
  public int label(String sentence, DatasetType datasetType) {
    for (String s : map.keySet()) {
      if (sentence.contains(s)) {
        return map.get(s);
      }
    }
    return -1;
  }

  @Override
  public void destroy() {
    map.clear();
    map = null;
    super.destroy();
  }
}
