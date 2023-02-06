package org.spider_man;

import okhttp3.ConnectionSpec;

import java.util.LinkedHashMap;
import java.util.List;

public class RequestTask {
    private String url;
    private String method = "GET";
    private LinkedHashMap<String, String> headers;
    private Object data;
    // 此处设置post请求是json还是form，如果是form设置为form
    private String dataType = "json";
    private boolean allow_redirects = true;
    private ProxyConfig proxies;
    private Integer timeout;
    private boolean verify = true;
    private String cert;
    private boolean isRandomUA = true;
    // 自定义SSL指纹
    List<ConnectionSpec> connectionSpecList;
    private boolean changeUserAgent = true;
    private Integer retryTimes = null;
    private List<Object> customParams;
    private String charSet;

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public boolean isChangeUserAgent() {
        return changeUserAgent;
    }

    public void setChangeUserAgent(boolean changeUserAgent) {
        this.changeUserAgent = changeUserAgent;
    }

    public boolean isRandomUA() {
        return isRandomUA;
    }

    public void setRandomUA(boolean randomUA) {
        isRandomUA = randomUA;
    }

    public List<ConnectionSpec> getConnectionSpecList() {
        return connectionSpecList;
    }

    public void setConnectionSpecList(List<ConnectionSpec> connectionSpecList) {
        this.connectionSpecList = connectionSpecList;
    }


    public List<Object> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(List<Object> customParams) {
        this.customParams = customParams;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(LinkedHashMap<String, String> headers) {
        this.headers = headers;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isAllow_redirects() {
        return allow_redirects;
    }

    public void setAllow_redirects(boolean allow_redirects) {
        this.allow_redirects = allow_redirects;
    }

    public ProxyConfig getProxies() {
        return proxies;
    }

    public void setProxies(ProxyConfig proxies) {
        this.proxies = proxies;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }
}
