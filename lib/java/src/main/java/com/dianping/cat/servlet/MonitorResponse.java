package com.dianping.cat.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class MonitorResponse extends HttpServletResponseWrapper {
    private int status;

    MonitorResponse(HttpServletResponse response) {
        super(response);
    }

    int getCurrentStatus() {
        return status;
    }

    @Override
    public void reset() {
        super.reset();
        status = 0;
    }

    @Override
    public void sendError(int error) throws IOException {
        super.sendError(error);
        this.status = error;
    }

    @Override
    public void sendError(int error, String message) throws IOException {
        super.sendError(error, message);
        this.status = error;
    }

    @Override
    public void setStatus(int status) {
        super.setStatus(status);
        this.status = status;
    }
}
