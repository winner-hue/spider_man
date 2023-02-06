package org.spider_man.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.ConnectionSpec;
import org.slf4j.Logger;
import org.spider_man.ProxyConfig;
import org.spider_man.requests.Requests;
import org.spider_man.requests.Response;
import org.spider_man.util.LoggerSpider;

import java.util.*;

public abstract class AbsSpider {
    private static Logger logger = LoggerSpider.getLogger(AbsSpider.class);
    public boolean isStop = false;
    public String spiderName = "spider_man";

    // 处理返回结果
    public abstract JSONArray process(Response response, List<Object> customParams);

    public abstract void storage(JSONArray object, Response response, List<Object> customParams);
    // 配置爬虫任务
//    public abstract List<RequestTask> configRequestTask();

    public Set<Integer> configStatusCode() {
        Set<Integer> set = new HashSet<>();
        set.add(200);
        set.add(201);
        set.add(403);
        set.add(503);
        set.add(404);
        set.add(202);
        set.add(301);
        set.add(302);
        set.add(500);
        return set;
    }

    // 配置多代理, 配置多代理，当其中一个代理失败后，将自动切换其他代理
    public HashMap<ProxyConfig, Boolean> configProxies() {
        return null;
    }

    // 测试代理可用性，仅支持setProxies
    public void monitorProxies() {
        HashMap<ProxyConfig, Boolean> config = configProxies();
        if (config != null) {
            while (!isStop) {
                for (ProxyConfig proxy : config.keySet()) {
                    if (config.get(proxy)) {
                        monitorProxy(proxy);
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception ignore) {

                }
            }
        }
    }

    public void sendMessage(String email, String message) {
        System.out.println(message);
    }

    public void monitorProxy(ProxyConfig proxy) {
        int monitorProxyInterval = proxy.getMonitorProxyInterval();
        if (proxy.getLastMonitorDate() == null || (System.currentTimeMillis() - proxy.getLastMonitorDate()) / 1000 > monitorProxyInterval) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("proxies", proxy);
            try {
                Requests.get(proxy.getTestUrl(), hashMap);
            } catch (Exception e) {
                sendMessage(proxy.getEmail(), "代理监控出现请求失败");
            }
        }
    }

    // 配置单代理
    public ProxyConfig configProxy() {
        return null;
    }

    // 配置URL连接代理,为每次请求Http获取
    public ProxyConfig configUrlProxy() {
        return null;
    }

    // 为所有请求配置请求头
    public LinkedHashMap<String, String> configHeaders() {
//        Headers.Builder builder = new Headers.Builder();
//        builder.add("user-agent", "");
//        return builder.build();
        return null;
    }

    // 爬虫请求前函数
    public void beforeRequest(String requestTaskUrl, String method, HashMap<String, Object> objectHashMap) {
        logger.info(requestTaskUrl + ": 开始请求");
    }

    // 爬虫返回数据后函数
    public void afterResponse(Response response) {
        logger.info(response.getReqUrl() + ": 请求结束");
    }

    // 爬虫启动前函数
    public void beforeSpider(AbsSpider spider) {
        logger.info(spider.getSpiderName() + ": 启动");
    }

    // 爬虫结束后函数
    public void afterSpider(AbsSpider spider) {
        logger.info(spider.getSpiderName() + ": 结束");
    }

    // 设置http请求类型
    // 可返回 http1.1, http2, quic
//    public String configHttpType() {
//        return null;
//    }

    // 设置全局请求类型，默认为okhttp，否则为httpclient
    public String configRequestPackage() {
//        可使用返回值 "okhttp", "httpclient"
        return "okhttp";
    }


    // 设置请求超时时间,单位秒
    public Integer configTimeout() {
        return 10;
    }

    // 设置下载失败后重试次数
    public Integer configRetryTimes() {
        return 3;
    }

    // 设置SSL指纹，请求类型必须是okhttp
    public List<ConnectionSpec> configSSLFinger() {
        return null;
    }

    public Integer configThreadNum() {
        return 10;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public String getSpiderName() {
        return spiderName;
    }

    public void setSpiderName(String spiderName) {
        this.spiderName = spiderName;
    }

    public boolean configTaskDup() {
        return false;
    }

}
