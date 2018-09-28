package com.dianping.cat.status.http;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class HttpStats {
    private static HttpStats current = null;
    private AtomicInteger httpStatus400Count = new AtomicInteger();
    private AtomicInteger httpStatus500Count = new AtomicInteger();
    private AtomicInteger httpCount = new AtomicInteger();
    private AtomicLong httpTimeSum = new AtomicLong();

    public static HttpStats currentStatsHolder() {
        if (null == current) {
            synchronized (HttpStats.class) {
                if (null == current) {
                    current = new HttpStats();
                }
            }
        }
        return current;
    }

    public static synchronized HttpStats getAndReset() {
        HttpStats tmp = new HttpStats();
        HttpStats old = currentStatsHolder();
        current = tmp;
        return old;
    }

    private HttpStats() {
    }

    public void doRequestStats(long mills, int status) {
        try {
            if (is400(status)) {
                httpStatus400Count.incrementAndGet();
            } else if (is500(status)) {
                httpStatus500Count.incrementAndGet();
            }
            httpCount.incrementAndGet();
            httpTimeSum.addAndGet(mills);
        } catch (Exception e) {
            // ignore
        }
    }

    public int getHttpCount() {
        return httpCount.get();
    }

    public int getHttpMeantime() {
        long meantime = 0 == httpCount.get() ? 0 : (httpTimeSum.get() / httpCount.get());
        return (int) meantime;
    }

    public int getHttpStatus400Count() {
        return httpStatus400Count.get();
    }

    public int getHttpStatus500Count() {
        return httpStatus500Count.get();
    }

    private boolean is400(int status) {
        return status >= 400 && status < 500;
    }

    private boolean is500(int status) {
        return status >= 500;
    }

}
