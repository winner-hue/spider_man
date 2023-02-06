package org.spider_man.execute;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.spider_man.spider.MysqlSpider;
import org.spider_man.util.LoggerSpider;
import org.spider_man.util.MysqlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class MysqlStoreExecute implements Runnable {
    private static Logger logger = LoggerSpider.getLogger(MysqlStoreExecute.class, "logs/store.log");
    private MysqlSpider mysqlSpider;

    public MysqlStoreExecute(MysqlSpider mysqlSpider) {
        this.mysqlSpider = mysqlSpider;
    }

    private static void process(LinkedBlockingQueue<HashMap<JSONArray, String>> result, Integer count) {
        List<String> sqlList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                HashMap<JSONArray, String> poll = result.poll();
                if (poll != null) {
                    for (Map.Entry<JSONArray, String> next : poll.entrySet()) {
                        JSONArray jsonArray = next.getKey();
                        String table = next.getValue();
                        if (jsonArray != null) {
                            for (int j = 0; j < jsonArray.size(); j++) {
                                JSONObject data = jsonArray.getJSONObject(j);
                                StringBuilder builder = new StringBuilder("insert into ");
                                builder.append(table);
                                StringBuilder keysBuilder = new StringBuilder();
                                StringBuilder valueBuilder = new StringBuilder();
                                for (Map.Entry<String, Object> storeData : data.entrySet()) {
                                    String value = storeData.getValue() == null ? "" : storeData.getValue().toString();
                                    String column = storeData.getKey();
                                    keysBuilder.append(column);
                                    keysBuilder.append(",");
                                    valueBuilder.append("'");
                                    value = value.replace("'", "\\'");
                                    valueBuilder.append(value);
                                    valueBuilder.append("'");
                                    valueBuilder.append(",");
                                }
                                builder.append("(");
                                keysBuilder.delete(keysBuilder.length() - 1, keysBuilder.length());
                                valueBuilder.delete(valueBuilder.length() - 1, valueBuilder.length());
                                builder.append(keysBuilder.toString());
                                builder.append(")");
                                builder.append(" values(");
                                builder.append(valueBuilder.toString());
                                builder.append(")");
                                sqlList.add(builder.toString());
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("获取队列数据失败：" + e);
            }
        }
        boolean insertAllRight = MysqlUtil.batchInsertSql(sqlList);
        if (!insertAllRight) {
            logger.info("批量插入失败，进行单条插入。。。");
            for (String sql : sqlList) {
                logger.info("插入sql:\n" + sql);
                MysqlUtil.insertSql(sql);
            }
        }
    }

    @Override
    public void run() {
        while (!mysqlSpider.isStop) {
            if (mysqlSpider.getResult().size() > 10) {
                process(mysqlSpider.getResult(), 10);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error("休眠失败：" + e);
            }
        }
        if (mysqlSpider.getResult().size() > 0) {
            process(mysqlSpider.getResult(), mysqlSpider.getResult().size());
        }
    }
}
