package org.spider_man.requests;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;

public class SSLSocketClient {
    private static class SSLSocketFactoryEx extends org.apache.http.conn.ssl.SSLSocketFactory {

        SSLContext sslContext = SSLContext.getInstance("TLS");

        public SSLSocketFactoryEx(KeyStore truststore, char[] arry)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            KeyManagerFactory localKeyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory
                            .getDefaultAlgorithm());
            localKeyManagerFactory.init(truststore, arry);
            KeyManager[] arrayOfKeyManager =
                    localKeyManagerFactory.getKeyManagers();
            TrustManager tm = new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {

                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {

                }
            };

            sslContext.init(arrayOfKeyManager, new TrustManager[]{tm},
                    new java.security.SecureRandom());
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port,
                    autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }


    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TrustManager[] getTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }

    public static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> true;
    }

    public static X509TrustManager getX509TrustManager() {
        X509TrustManager trustManager = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalAccessException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            trustManager = (X509TrustManager) trustManagers[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trustManager;
    }

    public static SSLConnectionSocketFactory getHttpclientSSLVerify() {
        return new SSLConnectionSocketFactory(getSSLSocketFactory(), NoopHostnameVerifier.INSTANCE);
    }

    public static SSLContext getSslContext(String cert, String password) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream stream = new FileInputStream(cert); // 读取证书
            Certificate certificate = factory.generateCertificate(stream); // 加载证书
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // 可以用JKS
            keyStore.load(null, null);  // load keyStore
            keyStore.setCertificateEntry("proxy_cert", certificate); // 证书命名
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            if (password != null) {
                keyManagerFactory.init(keyStore, password.toCharArray());  // 设置密码
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static SSLSocketFactory getSSLSocketFactoryCertO(String cert, String password) {
        try {
            return Objects.requireNonNull(getSslContext(cert, password)).getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SSLConnectionSocketFactory getSSLSocketFactoryCertH(String cert, String password) {
        try {
            return new SSLConnectionSocketFactory(Objects.requireNonNull(getSslContext(cert, password)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

