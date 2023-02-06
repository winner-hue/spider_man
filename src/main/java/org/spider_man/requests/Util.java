package org.spider_man.requests;

import okhttp3.ConnectionSpec;
import okhttp3.TlsVersion;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Util {
    public final static Random random = new Random();

    public static String randomSSL(String ssl) {
        String[] sslArray = ssl.split(",");
        List<String> list = Arrays.asList(sslArray);
        List<String> newLIst = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int i1 = random.nextInt(list.size());
            newLIst.add(list.get(i1));
        }
        return StringUtils.join(newLIst, ",");
    }

    public static List<ConnectionSpec> randomSSL() {
        String ssl = randomSSL("TLS_RSA_WITH_AES_256_GCM_SHA384," +
                "TLS_RSA_WITH_AES_128_GCM_SHA256," +
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256," +
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256," +
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384," +
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256," +
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256," +
                "TLS_RSA_WITH_3DES_EDE_CBC_SHA," +
                "TLS_RSA_WITH_AES_128_CBC_SHA," +
                "TLS_RSA_WITH_AES_256_CBC_SHA," +
                "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA," +
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA," +
                "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA," +
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");

        ConnectionSpec compatibleTLS = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
                .cipherSuites(ssl)
                .build();

        ssl = randomSSL("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256," +
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256," +
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256");
        ConnectionSpec modernTLS = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
                .cipherSuites(ssl)
                .build();

        List<ConnectionSpec> connectionSpecList = new ArrayList<>();
        connectionSpecList.add(modernTLS);
        connectionSpecList.add(compatibleTLS);
        return connectionSpecList;
    }
}
