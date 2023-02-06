package org.spider_man.requests;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import okhttp3.internal.platform.Platform;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.spider_man.ProxyConfig;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Requests {

    public static Response get(String url, HashMap<String, Object> objectHashMap) {
        return request(url, "GET", objectHashMap);
    }

    public static Response post(String url, HashMap<String, Object> objectHashMap) {
        return request(url, "POST", objectHashMap);
    }

    public static Response request(String url, String method, HashMap<String, Object> objectHashMap) {
        boolean verify = objectHashMap.get("verify") == null || Boolean.parseBoolean(objectHashMap.get("verify").toString());
        boolean allow_redirects = objectHashMap.get("allow_redirects") == null || Boolean.parseBoolean(objectHashMap.get("allow_redirects").toString());
        if (objectHashMap.get("client_type") == null || objectHashMap.get("client_type").toString().equalsIgnoreCase("okhttp")) {
            return okHttpResponse(url, method, verify, allow_redirects, objectHashMap);
        } else {
            return httpClientResponse(url, method, verify, allow_redirects, objectHashMap);
        }
    }

    public static ProxyConfig getProxy(ProxyConfig proxy) {
        Response response;
        try {
            for (int i = 0; i < proxy.getUrlProxyRetryNum(); i++) {
                response = Requests.get(proxy.getProxyUrl(), new HashMap<>());
                if (response.getStatusCode() == 200) {
                    JSONObject object = JSONObject.parseObject(response.getText());
                    String ip = object.getString("ip");
                    int port = object.getInteger("port");
                    String username = object.getString("username");
                    String password = object.getString("password");
                    ProxyConfig proxyConfig = new ProxyConfig();
                    proxyConfig.setHost(ip);
                    proxyConfig.setPassword(password);
                    proxyConfig.setPort(port);
                    proxyConfig.setUsername(username);
                    return proxyConfig;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response okHttpResponse(String url, String method, boolean verify, boolean allow_redirects, @NotNull HashMap<String, Object> objectHashMap) {
        OkHttpClient client = null;
        ProxyConfig proxies = objectHashMap.get("proxies") == null ? null : (ProxyConfig) objectHashMap.get("proxies");
        int timeout = objectHashMap.get("timeout") == null ? 10 : Integer.parseInt(objectHashMap.get("timeout").toString());
        String cert = objectHashMap.get("cert") == null ? null : objectHashMap.get("cert").toString();
        List<ConnectionSpec> connectionSpecList = objectHashMap.get("connection_specList") != null ? (List<ConnectionSpec>) objectHashMap.get("connectionSpecList") : null;
        if (proxies != null) {
            if (proxies.getProxyUrl() != null) {
                ProxyConfig proxy = getProxy(proxies);
                assert proxy != null;
                if (proxy.getUsername() != null && proxy.getPassword() != null) {
                    client = initClient(proxy.getUsername(), proxy.getPassword(), proxy.getHost(), proxy.getPort(), proxy.getProxyType(), timeout, allow_redirects, connectionSpecList, verify, cert);
                } else {
                    initClient(null, null, proxy.getHost(), proxy.getPort(), proxy.getProxyType(), timeout, allow_redirects, connectionSpecList, verify, cert);
                }
            } else {
                if (proxies.getUsername() != null && proxies.getPassword() != null) {
                    client = initClient(proxies.getUsername(), proxies.getPassword(), proxies.getHost(), proxies.getPort(), proxies.getProxyType(), timeout, allow_redirects, connectionSpecList, verify, cert);
                } else {
                    client = initClient(null, null, proxies.getHost(), proxies.getPort(), proxies.getProxyType(), timeout, allow_redirects, connectionSpecList, verify, cert);
                }
            }
        } else {
            client = initClient(null, null, null, null, null, timeout, allow_redirects, connectionSpecList, verify, cert);
        }
        Request request;
        okhttp3.Headers headers;
        if (objectHashMap.get("headers") != null) {
            LinkedHashMap<String, String> headersMap = (LinkedHashMap<String, String>) objectHashMap.get("headers");
            okhttp3.Headers.Builder builder = new okhttp3.Headers.Builder();
            for (String key : headersMap.keySet()) {
                builder.add(key, headersMap.get(key));
            }
            headers = builder.build();
        } else {
            headers = new okhttp3.Headers.Builder().build();
        }

        if (method.equalsIgnoreCase("get")) {
            request = new Request.Builder().url(url).headers(headers).get()
                    .build();

        } else {
            if (objectHashMap.get("data_type") == null || "json".equalsIgnoreCase(objectHashMap.get("data_type").toString())) {
                request = new Request.Builder().url(url).headers(headers)
                        .post(RequestBody.create(objectHashMap.get("data").toString(), MediaType.parse("application/json; charset=utf-8")))
                        .build();
            } else {
                FormBody body = (FormBody) objectHashMap.get("data");
                request = new Request.Builder().url(url).headers(headers)
                        .post(body)
                        .build();
            }
        }
        assert client != null;
        Call call = client.newCall(request);
        okhttp3.Response response;
        try {
            response = call.execute();
            Response resp = new Response();
            ResponseBody body = response.body();
            resp.setReqUrl(url);
            resp.setResponseContent(Objects.requireNonNull(body).bytes());
            resp.setText(new String(resp.getResponseContent(), objectHashMap.get("char-set").toString()));
            resp.setStatusCode(response.code());
            resp.setPostData(objectHashMap.get("data"));
            resp.setProxyConfig(proxies);
            List<String> headerList = response.headers("set-cookie");
            List<String> cookies = new ArrayList<>();
            HashMap<String, String> cookiesMap = new HashMap<>();

            for (String s : headerList) {
                String cookie = s.split(";")[0];
                cookies.add(cookie);
                if (cookie.split("=").length > 1) {
                    cookiesMap.put(cookie.split("=")[0], cookie.split("=")[1]);
                } else {
                    cookiesMap.put(cookie.split("=")[0], "");
                }
            }
            resp.setCookieMap(cookiesMap);
            resp.setCookie(StringUtils.join(cookies, "; "));
            resp.setReqHeaders(objectHashMap.get("headers") == null ? null : (LinkedHashMap<String, String>) objectHashMap.get("headers"));
            Headers respHeaders = response.headers();
            LinkedHashMap<String, String> respHeadersMap = new LinkedHashMap<String, String>();
            for (String name : respHeaders.names()) {
                respHeadersMap.put(name, respHeaders.get(name));
            }
            resp.setRedirectUrl(response.request().url().url().toString());
            resp.setResponseHeaders(respHeadersMap);
            client.connectionPool().evictAll();
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response httpClientResponse(String url, String method, boolean verify, boolean allow_redirects, HashMap<String, Object> objectHashMap) {
        Response resp = new Response();
        HttpClient client;
        RequestConfig config;
        int timeout = objectHashMap.get("timeout") == null ? 10 : Integer.parseInt(objectHashMap.get("timeout").toString());
        ProxyConfig proxies = objectHashMap.get("proxies") == null ? null : (ProxyConfig) objectHashMap.get("proxies");
        String cert = objectHashMap.get("cert") == null ? null : objectHashMap.get("cert").toString();
        if (proxies != null) {
            if (proxies.getProxyUrl() != null) {
                ProxyConfig proxy = getProxy(proxies);
                assert proxy != null;
                if (proxy.getUsername() != null && proxy.getPassword() != null) {
                    client = initClient(proxy.getUsername(), proxy.getPassword(), proxy.getHost(), proxy.getPort(), verify, cert);
                } else {
                    client = initClient(null, null, proxy.getHost(), proxy.getPort(), verify, cert);
                }
                config = initConfig(proxies.getHost(), proxies.getPort(), timeout, allow_redirects, proxies.getProxyType());
            } else {
                config = initConfig(proxies.getHost(), proxies.getPort(), timeout, allow_redirects, proxies.getProxyType());
                if (proxies.getUsername() != null && proxies.getPassword() != null) {
                    client = initClient(proxies.getUsername(), proxies.getPassword(), proxies.getHost(), proxies.getPort(), verify, cert);
                } else {
                    client = initClient(null, null, proxies.getHost(), proxies.getPort(), verify, cert);
                }
            }
        } else {
            client = initClient(null, null, null, null, verify, cert);
            config = initConfig(null, null, timeout, allow_redirects, null);
        }
        LinkedHashMap<String, String> headersMap = new LinkedHashMap<>();
        if (objectHashMap.get("headers") != null) {
            headersMap = (LinkedHashMap<String, String>) objectHashMap.get("headers");
        }

        if (method.equalsIgnoreCase("get")) {
            HttpGet get = new HttpGet(url);
            for (String key : headersMap.keySet()) {
                get.addHeader(key, headersMap.get(key));
            }
            try {
                return returnHttpclientResponse(get, config, client, resp, objectHashMap, proxies);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            HttpPost post = new HttpPost(url);
            for (String key : headersMap.keySet()) {
                post.addHeader(key, headersMap.get(key));
            }
            if (objectHashMap.get("data_type") == null || "json".equalsIgnoreCase(objectHashMap.get("data_type").toString())) {
                String dataData = objectHashMap.get("data").toString();
                try {
                    post.setEntity(new StringEntity(dataData));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                List<NameValuePair> postParams = (List<NameValuePair>) objectHashMap.get("data");
                try {
                    post.setEntity(new UrlEncodedFormEntity(postParams));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            try {
                return returnHttpclientResponse(post, config, client, resp, objectHashMap, proxies);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private static Response returnHttpclientResponse(HttpRequestBase request, RequestConfig config, HttpClient client, Response resp, HashMap<String, Object> objectHashMap, ProxyConfig proxies) throws IOException {
        request.setConfig(config);
        HttpClientContext context = HttpClientContext.create();
        HttpResponse execute = client.execute(request, context);
        resp.setResponseContent(EntityUtils.toByteArray(execute.getEntity()));
        String html = new String(resp.getResponseContent(), objectHashMap.get("char-set").toString());
        resp.setText(html);
        resp.setStatusCode(execute.getStatusLine().getStatusCode());
        Header[] headerList = execute.getHeaders("Set-Cookie");
        List<String> cookies = new ArrayList<>();
        HashMap<String, String> cookiesMap = new HashMap<>();
        for (Header s : headerList) {
            String cookie = s.getValue().split(";")[0];
            cookies.add(cookie);
            if (cookie.split("=").length > 1) {
                cookiesMap.put(cookie.split("=")[0], cookie.split("=")[1]);
            } else {
                cookiesMap.put(cookie.split("=")[0], "");
            }
        }
        resp.setCookie(StringUtils.join(cookies, "; "));
        resp.setReqUrl(request.getURI().toURL().toString());
        resp.setPostData(objectHashMap.get("data"));
        resp.setProxyConfig(proxies);
        resp.setCookieMap(cookiesMap);

        resp.setReqHeaders(objectHashMap.get("headers") == null ? null : (LinkedHashMap<String, String>) objectHashMap.get("headers"));
        Header[] respHeaders = execute.getAllHeaders();
        LinkedHashMap<String, String> respHeadersMap = new LinkedHashMap<String, String>();
        for (Header name : respHeaders) {
            respHeadersMap.put(name.getName(), name.getValue());
        }
        if (context.getRedirectLocations() != null) {
            resp.setRedirectUrl(context.toString());
        }
        resp.setResponseHeaders(respHeadersMap);
        return resp;
    }

    private static RequestConfig initConfig(String proxy_host, Integer proxy_port, int timeout, boolean allow_redirects, Proxy.Type proxy) {
        RequestConfig.Builder config = RequestConfig.custom();
        config.setRedirectsEnabled(allow_redirects);
        config.setConnectTimeout(timeout * 1000);
        config.setConnectionRequestTimeout(timeout);
        if (proxy_host != null && proxy_port != null) {
            config.setProxy(new HttpHost(proxy_host, proxy_port, proxy.toString()));
        }
        return config.build();
    }

    private static HttpClient initClient(String proxy_user, String proxy_pwd, String proxy_host, Integer proxy_port, boolean verify, String cert) {
        HttpClientBuilder client = HttpClients.custom();
        if (!verify) {
            client.setSSLSocketFactory(SSLSocketClient.getHttpclientSSLVerify());
        }
        if (proxy_user != null && proxy_pwd != null) {
            HttpHost host = new HttpHost(proxy_host, proxy_port);
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(proxy_user, proxy_pwd));
            client.setDefaultCredentialsProvider(provider);
        }
        if (cert != null) {
            String[] split = cert.split(";;");

            SSLConnectionSocketFactory sslSocketFactoryCertH;
            if (split.length > 1) {
                sslSocketFactoryCertH = SSLSocketClient.getSSLSocketFactoryCertH(split[0], split[1]);
            } else {
                sslSocketFactoryCertH = SSLSocketClient.getSSLSocketFactoryCertH(split[0], null);
            }
            client.setSSLSocketFactory(sslSocketFactoryCertH);
        }
        return client.build();
    }

    private static OkHttpClient initClient(String proxy_user, String proxy_pwd, String proxy_host, Integer proxy_port, Proxy.Type type, int timeout, boolean allow_redirects, List<ConnectionSpec> connectionSpecList, boolean verify, String cert) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.followRedirects(allow_redirects);
        builder.protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2, Protocol.QUIC));
        if (!verify) {
            builder.hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager());
        } else {
            if (connectionSpecList != null) {
                builder.connectionSpecs(connectionSpecList);
            }
            if (cert != null) {
                String[] split = cert.split(";;");
                if (split.length > 1) {
                    SSLSocketFactory sslSocketFactoryCert = SSLSocketClient.getSSLSocketFactoryCertO(split[0], split[1]);
                    if (sslSocketFactoryCert != null) {
                        builder.hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                                .sslSocketFactory(sslSocketFactoryCert, Objects.requireNonNull(Platform.get().trustManager(sslSocketFactoryCert)));
                    }
                } else {
                    SSLSocketFactory sslSocketFactoryCert = SSLSocketClient.getSSLSocketFactoryCertO(split[0], null);
                    if (sslSocketFactoryCert != null) {
                        builder.hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                                .sslSocketFactory(sslSocketFactoryCert, Objects.requireNonNull(Platform.get().trustManager(sslSocketFactoryCert)));
                    }
                }
            }
        }
        if (proxy_user != null) {
            builder
                    .followRedirects(allow_redirects)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2, Protocol.QUIC))
                    .proxy(new Proxy(type, new InetSocketAddress(proxy_host, proxy_port)))
                    .proxyAuthenticator((route, response) -> {
                        String credential = Credentials.basic(proxy_user, proxy_pwd);
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    })
                    .build();
        } else if (proxy_host != null) {
            builder
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .followRedirects(allow_redirects)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2, Protocol.QUIC))
                    .proxy(new Proxy(type, new InetSocketAddress(proxy_host, proxy_port)))
                    .build();

        } else {
            builder
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .followRedirects(allow_redirects)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2, Protocol.QUIC))
                    .build();
        }
        return builder.build();
    }

    public static void main(String[] args) throws IOException {
//        Response response = Requests.get("https://www.gstatic.cn/images/icons/material/system/2x/settings_grey600_24dp.png", new HashMap<>());
//
//
//        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("test.png"));
//        outputStream.write(response.responseContent);
//        outputStream.flush();
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("client_type", "");
        Response response = Requests.get("https://www.baidu.com", objectObjectHashMap);
        System.out.println(response.getCookie());
        System.out.println(response.getRedirectUrl());
        System.out.println(response.getResponseHeaders());
        System.out.println(response.getStatusCode());
        System.out.println(response.getText());
    }
}
