package org.spider_man.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.spider_man.RequestTask;
import org.spider_man.dispatch.StoreDispatch;
import org.spider_man.requests.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BaiduSpider extends MysqlSpider {

    public BaiduSpider(List<RequestTask> requestTasks) {
        super(requestTasks);
    }

    @Override
    public String configMysqlUser() {
        return "root";
    }

    @Override
    public String configMysqlPassword() {
        return "root";
    }

    @Override
    public String configMysqlTable() {
        return "baidu_news";
    }

    public static void main(String[] args) {
        List<RequestTask> requestTasks = new ArrayList<>();
        RequestTask requestTask = new RequestTask();
        requestTask.setUrl("https://baijiahao.baidu.com/s?id=1751873397233943113");
        requestTasks.add(requestTask);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        requestTask.setHeaders(map);
        new StoreDispatch().runScript(new BaiduSpider(requestTasks));
    }

    @Override
    public JSONArray process(Response response, List<Object> customParams) {
        JSONArray ja = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        Document document = Jsoup.parse(response.text);
        String title = document.select("div._28fPT ").text();
        String content = document.select("div._2Zphx ").text();
        String date = document.select("span._10s4U").text();
        jsonObject.put("title", title);
        jsonObject.put("content", content);
        jsonObject.put("date", date);
        ja.add(jsonObject);
        return ja;
    }

}
