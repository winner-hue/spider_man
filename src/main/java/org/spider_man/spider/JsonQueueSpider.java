package org.spider_man.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.spider_man.RequestTask;
import org.spider_man.requests.Response;
import org.spider_man.util.LoggerSpider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class JsonQueueSpider extends QueueSpider {
    private static final Logger logger = LoggerSpider.getLogger(JsonQueueSpider.class);
    private static OutputStreamWriter writer = null;

    public JsonQueueSpider(List<RequestTask> requestTasks) {
        super(requestTasks);
    }

    protected void storage(File file, JSONArray object) throws IOException {
        if (writer == null) {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        }
        writer.write(object.toJSONString());
        writer.write("\n");
        writer.flush();
    }

    @Override
    public void storage(JSONArray object, Response response, List<Object> customParams) {
        if (object != null) {
            logger.info(object.toJSONString());
            File file = new File("store/spider_man.json");
            if (!file.exists()) {
                try {
                    boolean newFile = file.createNewFile();
                    if (newFile) {
                        storage(file, object);
                    }
                } catch (IOException e) {
                    logger.error("存储失败：" + e);
                }
            } else {
                try {
                    storage(file, object);
                } catch (Exception e) {
                    logger.error("存储文件失败：" + e);
                }
            }
        } else {
            logger.warn("提取内容为空，不存储");
        }
    }

    @Override
    public void afterSpider(AbsSpider spider) {
        super.afterSpider(spider);
        try {
            writer.close();
        } catch (IOException e) {
            logger.error("关闭文件失败：" + e);
        }
    }
}
