package com.smzdm.elasticsearch.http.jetty;

import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhengwen.zhu
 */
public class HttpServerRestChannel extends HttpChannel {
    private final RestRequest restRequest;

    private final HttpServletResponse resp;

    private IOException sendFailure;

    private final CountDownLatch latch;

    public HttpServerRestChannel(RestRequest restRequest, HttpServletResponse resp) {
        super(restRequest, true);
        this.restRequest = restRequest;
        this.resp = resp;
        this.latch = new CountDownLatch(1);
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public IOException sendFailure() {
        return sendFailure;
    }

    @Override
    public void sendResponse(RestResponse response) {
        resp.setContentType(response.contentType());
        resp.addHeader("Access-Control-Allow-Origin", "*");
        if (response.status() != null) {
            resp.setStatus(response.status().getStatus());
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if (restRequest.method() == RestRequest.Method.OPTIONS) {
            // also add more access control parameters
            resp.addHeader("Access-Control-Max-Age", "1728000");
            resp.addHeader("Access-Control-Allow-Methods", "OPTIONS, HEAD, GET, POST, PUT, DELETE");
            resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Content-Length");
        }
        ServletOutputStream out = null;
        try {
            int contentLength = response.content().length();
            resp.setContentLength(contentLength);
            out = resp.getOutputStream();
            response.content().writeTo(out);
        } catch (IOException e) {
            sendFailure = e;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    //ignore
                }
            }
            latch.countDown();
        }
     }
}
