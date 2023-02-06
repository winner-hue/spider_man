package org.spider_man.execute;

import com.alibaba.fastjson.JSONArray;
import okhttp3.ConnectionSpec;
import org.slf4j.Logger;
import org.spider_man.ProxyConfig;
import org.spider_man.RequestTask;
import org.spider_man.requests.Requests;
import org.spider_man.requests.Response;
import org.spider_man.spider.AbsSpider;
import org.spider_man.util.LoggerSpider;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SpiderExecute implements Runnable {
    private static Logger logger = LoggerSpider.getLogger(SpiderExecute.class);
    private RequestTask requestTask;
    private AbsSpider spider;

    public SpiderExecute(RequestTask requestTask, AbsSpider absSpider) {
        this.requestTask = requestTask;
        this.spider = absSpider;
    }

    private static void addProxies(HashMap<String, Object> objectHashMap, HashMap<ProxyConfig, Boolean> proxyConfigBooleanHashMap, ProxyConfig configProxy, ProxyConfig configUrlProxy) {
        ProxyConfig proxyRequestConfig = null;
        if (proxyConfigBooleanHashMap != null && proxyConfigBooleanHashMap.size() > 0) {
            for (ProxyConfig proxyConfig : proxyConfigBooleanHashMap.keySet()) {
                if (proxyConfigBooleanHashMap.get(proxyConfig)) {
                    proxyRequestConfig = proxyConfig;
                    break;
                }
            }
        }
        if (proxyRequestConfig == null) {
            if (configUrlProxy != null) {
                proxyRequestConfig = configUrlProxy;
            }
            if (configProxy != null) {
                proxyRequestConfig = configProxy;
            }
        }
        objectHashMap.put("proxies", proxyRequestConfig);
    }

    private static void addHeaders(HashMap<String, Object> objectHashMap, LinkedHashMap<String, String> configHeaders) {
        Object headers = objectHashMap.get("headers");
        if (headers != null) {
            LinkedHashMap<String, String> proxyHeaders = (LinkedHashMap<String, String>) headers;
            if (configHeaders != null) {
                for (Map.Entry next : configHeaders.entrySet()) {
                    Object key = next.getKey();
                    Object value = next.getValue();
                    proxyHeaders.put(key.toString(), value.toString());
                }
            }
        } else {
            headers = configHeaders;
        }
        objectHashMap.put("headers", headers);
    }

    private HashMap<String, Object> initRequestContent() {
        HashMap<String, Object> objectHashMap = new HashMap<>();
        // spider请求参数
        HashMap<ProxyConfig, Boolean> proxyConfigBooleanHashMap = spider.configProxies();
        ProxyConfig configProxy = spider.configProxy();
        ProxyConfig configUrlProxy = spider.configUrlProxy();
        LinkedHashMap<String, String> configHeaders = spider.configHeaders();
        String configRequestPackage = spider.configRequestPackage();
        List<ConnectionSpec> configSSLFinger = spider.configSSLFinger();
        Integer configTimeout = spider.configTimeout();

        addProxies(objectHashMap, proxyConfigBooleanHashMap, configProxy, configUrlProxy);
        addHeaders(objectHashMap, configHeaders);
        objectHashMap.put("client_type", configRequestPackage);
        if (configRequestPackage == null || "okhttp".equalsIgnoreCase(configRequestPackage)) {
            objectHashMap.put("connection_specList", configSSLFinger);
        }
        objectHashMap.put("timeout", configTimeout);
        objectHashMap.put("char-set", StandardCharsets.UTF_8);
        return objectHashMap;
    }

    private void updateRequestContent(HashMap<String, Object> objectHashMap) {
        String cert = requestTask.getCert();
        List<ConnectionSpec> connectionSpecList = requestTask.getConnectionSpecList();
        Object data = requestTask.getData();
        String dataType = requestTask.getDataType();
        LinkedHashMap<String, String> headers = requestTask.getHeaders();
        ProxyConfig proxies = requestTask.getProxies();
        Integer timeout = requestTask.getTimeout();
        String charSet = requestTask.getCharSet();
        objectHashMap.put("cert", cert);
        if (connectionSpecList != null) {
            objectHashMap.put("connection_specList", connectionSpecList);
        }
        objectHashMap.put("data", data);
        objectHashMap.put("data_type", dataType);
        addHeaders(objectHashMap, headers);
        if (proxies != null) {
            objectHashMap.put("proxies", proxies);
        }
        if (timeout != null) {
            objectHashMap.put("timeout", timeout);
        }
        if (charSet != null) {
            objectHashMap.put("char-set", charSet);
        }
    }

    private static boolean inStatusCodeSet(Set<Integer> statusCode, int status) {
        for (int tmpStatus : statusCode) {
            if (tmpStatus == status) {
                return true;
            }
        }
        return false;
    }

    private static boolean process(String requestTaskUrl, String method, HashMap<String, Object> objectHashMap, Set<Integer> statusCode, AbsSpider spider, List<Object> customParams) {
        boolean flag = false;
        try {
            spider.beforeRequest(requestTaskUrl, method, objectHashMap);
            Response response = Requests.request(requestTaskUrl, method, objectHashMap);
            spider.afterResponse(response);
            if (inStatusCodeSet(statusCode, response.getStatusCode())) {
                flag = true;
                JSONArray jsonArray = spider.process(response, customParams);
                spider.storage(jsonArray, response, customParams);
            }
        } catch (Exception e) {
            logger.error("下载错误或处理错误：" + e);
        }
        return flag;
    }

    @Override
    public void run() {
        Integer configRetryTimes = spider.configRetryTimes();
        Set<Integer> statusCode = spider.configStatusCode();
        Integer retryTimes = requestTask.getRetryTimes();
        List<Object> customParams = requestTask.getCustomParams();
        HashMap<String, Object> objectHashMap = initRequestContent();

        updateRequestContent(objectHashMap);
        String requestTaskUrl = requestTask.getUrl();
        String method = requestTask.getMethod();

        if (retryTimes == null && configRetryTimes == null) {
            process(requestTaskUrl, method, objectHashMap, statusCode, spider, customParams);
            return;
        }
        if (retryTimes != null) {
            for (int i = 0; i < retryTimes; i++) {
                if (process(requestTaskUrl, method, objectHashMap, statusCode, spider, customParams)) {
                    return;
                }
            }
            return;
        }
        for (int i = 0; i < configRetryTimes; i++) {
            if (process(requestTaskUrl, method, objectHashMap, statusCode, spider, customParams)) {
                return;
            }
        }
    }
}
