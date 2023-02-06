package org.spider_man.spider;

import com.alibaba.fastjson.JSONArray;
import org.spider_man.RequestTask;
import org.spider_man.requests.Response;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MysqlSpider extends QueueSpider {

    public LinkedBlockingQueue<HashMap<JSONArray, String>> result = new LinkedBlockingQueue<>();

    public MysqlSpider(List<RequestTask> requestTasks) {
        super(requestTasks);
    }

    public String configMysqlJDBCUrl() {
        return "jdbc:mysql://localhost:3306/spider_man";
    }

    public String configMysqlUser() {
        return "spider_man";
    }

    public String configMysqlPassword() {
        return "spider_man";
    }

    public String configMysqlTable() {
        return "spider_man";
    }

    @Override
    public void storage(JSONArray object, Response response, List<Object> customParams) {
        if (object != null && object.size() > 0) {
            HashMap<JSONArray, String> map = new HashMap<>();
            map.put(object, configMysqlTable());
            result.add(map);
        }
    }

    @Override
    public JSONArray process(Response response, List<Object> customParams) {
        return super.process(response, customParams);
    }

    public LinkedBlockingQueue<HashMap<JSONArray, String>> getResult() {
        return result;
    }

    public void setResult(LinkedBlockingQueue<HashMap<JSONArray, String>> result) {
        this.result = result;
    }

}
