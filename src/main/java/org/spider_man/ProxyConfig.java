package org.spider_man;

import java.net.Proxy;
import java.util.Date;

public class ProxyConfig {
    private String host = "127.0.0.1";
    private Integer port = 8080;
    private String username = null;
    private String password = null;
    private Proxy.Type proxyType = Proxy.Type.HTTP;
    // 代理是否激活，如果激活则为true，不激活则为false， 在代理检测中，
    // 如果发现代理不可用，则将其置为false,只有当代理为true的时候，才可以进行使用
    private boolean isActive = true;
    // 当代理isActive=false的时候，检测代理是否还会对其进行检测，
    // 如果为true，则继续检测，如果为false，则不再检测
    private boolean isTestFalseProxy = true;
    // 配置代理测试地址
    private String testUrl = "https://www.baidu.com";
    // 配置代理通知邮箱，当代理发生错误时，将通过该邮箱发送邮件,默认无配置
    private String email = null;
    // 配置代理检测间隔，默认为180秒
    private int monitorProxyInterval = 180;
    // 代理失败重试次数，仅针对URL模式代理
    private int urlProxyRetryNum = 3;
    //上次检查时间
    private Long lastMonitorDate = null;
    // 配置URL代理
    private String proxyUrl = null;
    // 是否忽略SSL认证
    private boolean isIgnoreSSLVerify = true;

    public int getUrlProxyRetryNum() {
        return urlProxyRetryNum;
    }

    public void setUrlProxyRetryNum(int urlProxyRetryNum) {
        this.urlProxyRetryNum = urlProxyRetryNum;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Proxy.Type getProxyType() {
        return proxyType;
    }

    public void setProxyType(Proxy.Type proxyType) {
        this.proxyType = proxyType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isTestFalseProxy() {
        return isTestFalseProxy;
    }

    public void setTestFalseProxy(boolean testFalseProxy) {
        isTestFalseProxy = testFalseProxy;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMonitorProxyInterval() {
        return monitorProxyInterval;
    }

    public void setMonitorProxyInterval(int monitorProxyInterval) {
        this.monitorProxyInterval = monitorProxyInterval;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public boolean isIgnoreSSLVerify() {
        return isIgnoreSSLVerify;
    }

    public void setIgnoreSSLVerify(boolean ignoreSSLVerify) {
        isIgnoreSSLVerify = ignoreSSLVerify;
    }

    public Long getLastMonitorDate() {
        return lastMonitorDate;
    }

    public void setLastMonitorDate(Long lastMonitorDate) {
        this.lastMonitorDate = lastMonitorDate;
    }
}
