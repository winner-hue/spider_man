package org.spider_man.requests;

import org.spider_man.ProxyConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Response {
    public int statusCode;
    public String text;
    public String cookie;
    public HashMap<String, String> cookieMap;
    public LinkedHashMap<String, String> responseHeaders;
    public byte[] responseContent;
    public String reqUrl;
    public LinkedHashMap<String, String> reqHeaders;
    public ProxyConfig proxyConfig;
    public String redirectUrl;
    public Object postData;

    public HashMap<String, String> getCookieMap() {
        return cookieMap;
    }

    public void setCookieMap(HashMap<String, String> cookieMap) {
        this.cookieMap = cookieMap;
    }

    public LinkedHashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(LinkedHashMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public byte[] getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(byte[] responseContent) {
        this.responseContent = responseContent;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public LinkedHashMap<String, String> getReqHeaders() {
        return reqHeaders;
    }

    public void setReqHeaders(LinkedHashMap<String, String> reqHeaders) {
        this.reqHeaders = reqHeaders;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Object getPostData() {
        return postData;
    }

    public void setPostData(Object postData) {
        this.postData = postData;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
