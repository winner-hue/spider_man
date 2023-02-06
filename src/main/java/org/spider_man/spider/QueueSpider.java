package org.spider_man.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.spider_man.RequestTask;
import org.spider_man.requests.Response;
import org.spider_man.util.LoggerSpider;

import java.util.List;

public class QueueSpider extends AbsSpider {
    private static Logger logger = LoggerSpider.getLogger(QueueSpider.class);
    protected int queueSize = Integer.MAX_VALUE;

    public List<RequestTask> requestTasks;

    public QueueSpider(List<RequestTask> requestTasks) {
        this.requestTasks = requestTasks;
    }

    @Override
    public JSONArray process(Response response, List<Object> customParams) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("html", response.getText());
        jsonArray.add(jsonObject);
        return jsonArray;
    }

    @Override
    public void storage(JSONArray object, Response response, List<Object> customParams) {
        if (object != null) {
            logger.info(object.toJSONString());
        } else {
            logger.warn("提取内容为空，不存储");
        }
    }


    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public List<RequestTask> getRequestTasks() {
        return requestTasks;
    }

    public void addRequestTask(RequestTask requestTasks) {
        this.requestTasks.add(requestTasks);
    }

    public void addRequestsTasks(List<RequestTask> requestTasks) {
        this.requestTasks.addAll(requestTasks);
    }
}
