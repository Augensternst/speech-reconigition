package com.example.backend.config;

import org.java_websocket.client.WebSocketClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SslConfig {
    public static void configureSslContext(WebSocketClient client) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[] {}; }
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    }
            }, new SecureRandom());
            client.setSocket(sc.getSocketFactory().createSocket());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
