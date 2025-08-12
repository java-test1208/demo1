package com.example.demo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@Configuration
public class AppConfig {

    @Bean
    public OkHttpClient okHttpClient(MeterRegistry meterRegistry) throws IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        
        File cacheDirectory = new File("okhttp-cache");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(cacheDirectory, cacheSize);

        SslConfig sslConfig = createSslConfig("secret/mytruststore.jks", "changeit");

        OkHttpMetricsEventListener eventListener = OkHttpMetricsEventListener
                .builder(meterRegistry, "okhttp.requests")
                .build();

        return new OkHttpClient.Builder()
                .cache(cache)
                .eventListener(eventListener)
                .sslSocketFactory(sslConfig.sslSocketFactory, sslConfig.trustManager)
                .build();
    }

    private static SslConfig createSslConfig(String truststoreFile, String truststorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, java.security.KeyManagementException {


        KeyStore truststore = KeyStore.getInstance("JKS");
        try (InputStream in = new FileInputStream(truststoreFile)) {
            truststore.load(in, truststorePassword.toCharArray());
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(truststore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }

        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { trustManager }, null);

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return new SslConfig(sslSocketFactory, trustManager);
    }
    
    private static class SslConfig {

        final SSLSocketFactory sslSocketFactory;
        final X509TrustManager trustManager;

        SslConfig(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
            this.sslSocketFactory = sslSocketFactory;
            this.trustManager = trustManager;
        }
    }

}
