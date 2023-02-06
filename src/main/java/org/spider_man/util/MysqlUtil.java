package org.spider_man.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.spider_man.spider.MysqlSpider;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MysqlUtil {
    private static Logger logger = LoggerSpider.getLogger(MysqlUtil.class, "logs/store.log");
    private static HashSet<String> existTableName;
    public static Connection conn = null;
    public static Statement statement = null;

    synchronized public static void initMysqlClient(MysqlSpider mysqlSpider) {
        if (conn == null && statement == null) {
            String mysqlJDBCUrl = mysqlSpider.configMysqlJDBCUrl();
            String mysqlPassword = mysqlSpider.configMysqlPassword();
            String mysqlUser = mysqlSpider.configMysqlUser();
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(mysqlJDBCUrl, mysqlUser, mysqlPassword);
                statement = conn.createStatement();
                existTableName = new HashSet<>();
            } catch (Exception e) {
                logger.error("mysql初始化错误：" + e);
                logger.error("退出系统");
                System.exit(1);
            }
        }
    }

    synchronized public static void releaseMysqlClient() {
        existTableName.clear();
        try {
            statement.close();
            conn.close();
        } catch (SQLException e) {
            logger.error("关闭mysql失败：" + e);
        }
    }

    synchronized public static boolean isExistTable(String table) {
        if (existTableName.add(table)) {
            String sql = String.format("select * from information_schema.TABLES where TABLE_NAME = '%s'", table);
            try {
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    return true;
                } else {
                    existTableName.remove(table);
                    return false;
                }
            } catch (Exception e) {
                logger.error(String.format("表->%s 判断是否存在失败。。。" + e, table));
                existTableName.remove(table);
            }
        }
        return true;
    }

    synchronized public static boolean createCurrentTable(String table, List<String> params) {
        StringBuilder builder = new StringBuilder("id integer auto_increment not null primary key,\n");

        for (String param : params) {
            builder.append(param.trim());
            builder.append(" text,\n");
        }
        builder.delete(builder.length() - 2, builder.length() - 1);
        String sql = String.format("create table %s (%s)", table, builder.toString());
        try {
            statement.executeUpdate(sql);
            logger.info(String.format("创建表-> %s 成功...", table));
            return true;
        } catch (Exception e) {
            logger.error("创建表失败：" + e);
        }
        return false;
    }

    public static boolean insertSql(String sql, String table, List<String> params) {
        if (isExistTable(table) || createCurrentTable(table, params)) {
            try {
                return statement.execute(sql);
            } catch (SQLException e) {
                logger.error("插入数据失败：" + e);
            }
        }
        return false;
    }

    public static void insertSql(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error("插入数据失败：" + e);
        }
    }

    synchronized public static boolean createAllTable(List<String> stringList) {
        try {
            for (String sql : stringList) {
                String tableName = CommentUtil.re("insert\\s{0,10}into\\s+{0,10}(\\w+)\\s{0,10}", sql);
                List<String> paramsList = CommentUtil.reList("insert\\s{0,10}into\\s+{0,10}\\w+\\s{0,10}\\(([\\w,]+)?\\)", sql);
                String result = paramsList.get(1);
                if (isExistTable(tableName)) {
                    continue;
                }
                boolean currentTable = createCurrentTable(tableName, Arrays.asList(result.split(",")));
                if (currentTable) {
                    existTableName.add(tableName);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("sqlpaser出错：" + e);
            return false;
        }
        return true;
    }

    public static boolean batchInsertSql(List<String> stringList) {
        boolean allTable = createAllTable(stringList);
        if (allTable) {
            try {
                for (String sql : stringList) {
                    statement.addBatch(sql);
                }
                statement.executeBatch();
                logger.info("------------数据批量插入成功--------------");
                return true;
            } catch (Exception e) {
                logger.error("批量插入sql失败：" + e);
                return false;
            }
        }
        return false;
    }

    public static JSONArray selectSql(String sql) {
        JSONArray sqlArray = new JSONArray();
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    String column = metaData.getColumnName(i + 1);
                    Object object = resultSet.getObject(column);
                    jsonObject.put(column, object);
                }
                sqlArray.add(jsonObject);
            }
            return sqlArray;
        } catch (SQLException e) {
            logger.error("执行sql失败：" + e);
        }
        return null;
    }

}
