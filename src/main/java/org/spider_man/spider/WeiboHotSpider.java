package org.spider_man.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.spider_man.RequestTask;
import org.spider_man.dispatch.StoreDispatch;
import org.spider_man.requests.Response;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class WeiboHotSpider extends MysqlSpider {
    public WeiboHotSpider(List<RequestTask> requestTasks) {
        super(requestTasks);
    }

    @Override
    public String configMysqlJDBCUrl() {
        return "jdbc:mysql://localhost:3306/my_world";
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
        return "home_hotarticles";
    }

    public static void main(String[] args) {
        List<RequestTask> requestTasks = new ArrayList<>();
        RequestTask requestTask = new RequestTask();
        requestTask.setUrl("https://weibo.com/ajax/statuses/hot_band");
        requestTasks.add(requestTask);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        requestTask.setHeaders(map);
        new StoreDispatch().runScript(new WeiboHotSpider(requestTasks));
    }

    @Override
    public JSONArray process(Response response, List<Object> customParams) {
        JSONArray ja = new JSONArray();
        JSONObject jo = JSONObject.parseObject(response.text);
        Integer http_code = jo.getInteger("http_code");
        if (http_code == 200) {
            JSONObject data = jo.getJSONObject("data");
            JSONObject hotgov = data.getJSONObject("hotgov");
            if (hotgov != null) {
                String name = hotgov.getString("name");
                String url = hotgov.getString("url");
                int index = 0;
                String small_icon_desc = hotgov.getString("small_icon_desc");
                JSONObject result = new JSONObject();
                result.put("index_num", index);
                result.put("title", name);
                result.put("hot_value", null);
                result.put("article_url", url);
                result.put("flag", small_icon_desc);
                result.put("is_top", 1);
                ja.add(result);
            }
            JSONArray bandList = data.getJSONArray("band_list");
            if (bandList != null) {
                for (int i = 0; i < bandList.size(); i++) {
                    JSONObject jsonObject = bandList.getJSONObject(i);
                    String name = jsonObject.getString("note");
                    String small_icon_desc = jsonObject.getString("small_icon_desc");
                    int index = i + 1;
                    Integer num = jsonObject.getInteger("num");
                    String url = "https://s.weibo.com/weibo?q=%23" + URLEncoder.encode(name) + "%23";
                    JSONObject object = new JSONObject();
                    object.put("index_num", index);
                    object.put("title", name);
                    object.put("hot_value", num);
                    object.put("article_url", url);
                    object.put("flag", small_icon_desc);
                    object.put("is_top", 0);
                    ja.add(object);
                }
            }
        }
        return ja;
    }

}
