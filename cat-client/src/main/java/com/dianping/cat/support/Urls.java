package com.dianping.cat.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.dianping.cat.support.Files.AutoClose;

public class Urls {
   public static UrlIO forIO() {
      return new UrlIO();
   }

   public static class UrlIO {
      private int m_readTimeout;

      private int m_connectTimeout;

      private Map<String, String> m_headers = new HashMap<String, String>();

      private byte[] m_body;

      private boolean m_gzip;

      public UrlIO connectTimeout(int connectTimeout) {
         m_connectTimeout = connectTimeout;
         return this;
      }

      public void copy(String url, OutputStream out) throws IOException {
         Files.forIO().copy(openStream(url), out, AutoClose.INPUT);
      }

      public UrlIO header(String name, String value) {
         m_headers.put(name, value);
         return this;
      }

      public UrlIO body(byte[] body) {
         m_body = body;
         return this;
      }

      public InputStream openStream(String url) throws IOException {
         return openStream(url, null);
      }

      public InputStream openStream(String url, Map<String, List<String>> responseHeaders) throws IOException {
         URLConnection conn = new URL(url).openConnection();

         if (m_connectTimeout > 0) {
            conn.setConnectTimeout(m_connectTimeout);
         }

         if (m_readTimeout > 0) {
            conn.setReadTimeout(m_readTimeout);
         }

         if (!m_headers.isEmpty()) {
            for (Map.Entry<String, String> e : m_headers.entrySet()) {
               String name = e.getKey();
               String value = e.getValue();

               if (name != null && value != null) {
                  conn.setRequestProperty(name, value);
               }
            }
         }

         if (m_body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Length", String.valueOf(m_body.length));

            conn.getOutputStream().write(m_body);
         }

         conn.connect();

         Map<String, List<String>> headers = conn.getHeaderFields();

         if (responseHeaders != null) {
            responseHeaders.putAll(headers);
         }

         if (m_gzip && headers != null && "[gzip]".equals(String.valueOf(headers.get("Content-Encoding")))) {
            return new GZIPInputStream(conn.getInputStream());
         } else {
            return conn.getInputStream();
         }
      }

      public UrlIO readTimeout(int readTimeout) {
         m_readTimeout = readTimeout;
         return this;
      }

      public UrlIO withGzip() {
         m_gzip = true;
         m_headers.put("Accept-Encoding", "gzip");
         return this;
      }
   }
}
