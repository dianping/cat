package com.smzdm.elasticsearch.http.jetty.error;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhengwen.zhu
 */
public class JettyHttpServerErrorHandler extends ErrorHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
        connection.getRequest().setHandled(true);
        response.setContentType(MimeTypes.TEXT_PLAIN_8859_1);
        ByteArrayISO8859Writer writer= new ByteArrayISO8859Writer(4096);
        writer.write(request.getAttribute(Dispatcher.ERROR_STATUS_CODE) + " " +
                     request.getAttribute(Dispatcher.ERROR_MESSAGE) + " " +
                     request.getAttribute(Dispatcher.ERROR_REQUEST_URI));
        writer.flush();
        response.setContentLength(writer.size());
        writer.writeTo(response.getOutputStream());
        writer.destroy();
    }
}
