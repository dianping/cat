package com.dianping.cat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Urls {
    public static UrlIO forIO() {
        return new UrlIO();
    }

    public static class UrlIO {
        private int readTimeout;
        private int connectTimeout;
        private Map<String, String> headers = new HashMap<String, String>();

        public UrlIO connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public UrlIO header(String name, String value) {
            if (headers == null) {
                headers = new HashMap<String, String>();
            }

            headers.put(name, value);
            return this;
        }

        public void copy(String url, OutputStream out) throws IOException {
            Files.forIO().copy(openStream(url), out, Files.AutoClose.INPUT);
        }

        public InputStream openStream(String url) throws IOException {
            return openStream(url, null);
        }

        public InputStream openStream(String url, Map<String, List<String>> responseHeaders) throws IOException {
            URLConnection conn = new URL(url).openConnection();

            if (connectTimeout > 0) {
                conn.setConnectTimeout(connectTimeout);
            }

            if (readTimeout > 0) {
                conn.setReadTimeout(readTimeout);
            }

            if (headers != null) {
                for (Map.Entry<String, String> e : headers.entrySet()) {
                    conn.setRequestProperty(e.getKey(), e.getValue());
                }
            }

            if (responseHeaders != null) {
                responseHeaders.putAll(conn.getHeaderFields());
            }

            return conn.getInputStream();
        }

        public UrlIO readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
    }
}
