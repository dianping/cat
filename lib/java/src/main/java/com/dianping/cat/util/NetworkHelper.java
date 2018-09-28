package com.dianping.cat.util;

import com.dianping.cat.log.CatLogger;

import java.io.InputStream;

public class NetworkHelper {

    private static final int TIMEOUT = 2000;

    public static String readFromUrlWithRetry(String url) throws Exception {
        try {
            InputStream input = Urls.forIO().readTimeout(TIMEOUT).connectTimeout(TIMEOUT).openStream(url);

            return Files.forIO().readFrom(input, "utf-8");
        } catch (Exception e) {
            try {
                InputStream in = Urls.forIO().connectTimeout(TIMEOUT).readTimeout(TIMEOUT).openStream(url);

                return Files.forIO().readFrom(in, "utf-8");
            } catch (Exception retryException) {
                CatLogger.getInstance().error("error when read url:" + url + ",exception is " + retryException.getMessage());

                throw retryException;
            }
        }

    }
}
