package com.smzdm.elasticsearch.http.jetty.handler;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Transaction;
import com.smzdm.elasticsearch.http.jetty.HttpServerRestChannel;
import com.smzdm.elasticsearch.http.jetty.HttpServerRestRequest;
import com.smzdm.elasticsearch.http.jetty.JettyHttpServerTransport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.elasticsearch.common.Classes;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.http.HttpServerAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhengwen.zhu
 */
public class JettyHttpServerTransportHandler extends AbstractHandler {

    private volatile JettyHttpServerTransport transport;

    protected volatile ESLogger logger;

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        Server server = getServer();
        // JettyHttpServerTransport can be either set explicitly in jetty.xml or obtained from server
        if (transport == null) {
            JettyHttpServerTransport transport = (JettyHttpServerTransport) server.getAttribute(JettyHttpServerTransport.TRANSPORT_ATTRIBUTE);
            if (transport == null) {
                throw new IllegalArgumentException("Transport is not specified");
            }
            setTransport(transport);
        }
    }

    @Override
    protected void doStop() throws Exception {
        this.transport = null;
        this.logger = null;
        super.doStop();
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        Context context = new Context(request, response);
        Transaction t = logCatTransaction(context);

        HttpServerAdapter adapter = getTransport().httpServerAdapter();
        HttpServerRestRequest restRequest = new HttpServerRestRequest(request);
        HttpServerRestChannel restChannel = new HttpServerRestChannel(restRequest, response);
        try {
            adapter.dispatchRequest(restRequest, restChannel);
            restChannel.await();

            t.setStatus(Transaction.SUCCESS);
        } catch (InterruptedException e) {
            t.setStatus(e);
            throw new ServletException("failed to dispatch request", e);
        } catch (Exception e) {
            t.setStatus(e);
            throw new IOException("failed to dispatch request", e);
        } finally {
            t.complete();
        }
        if (restChannel.sendFailure() != null) {
            throw restChannel.sendFailure();
        }
    }


    public JettyHttpServerTransport getTransport() {
        return transport;
    }

    public void setTransport(JettyHttpServerTransport transport) {
        this.transport = transport;
        this.logger = Loggers.getLogger(buildClassLoggerName(getClass()), transport.settings());
    }

    private static String buildClassLoggerName(Class clazz) {
        return Classes.getPackageName(clazz);
    }

    private Transaction logCatTransaction(Context ctx) {
        Transaction t = null;
        String uri = ctx.getProperty(Context.ORIGIN_URL);

        if (StringUtils.isBlank(uri)) {
            uri = "/";
            ctx.addProperty(Context.ORIGIN_URL, uri);
        }
        if (StringUtils.isNotBlank(ctx.getProperty(Context.CALLER_DOMAIN))
                && StringUtils.isNotBlank(ctx.getProperty(Context.CALLER_METHOD))) {
            t = Cat.newTransaction("Service", ctx.getProperty(Context.CALLER_METHOD));
            Cat.logEvent("Service.client", ctx.getProperty(Context.REMOTE_IP));
            Cat.logEvent("Service.app", ctx.getProperty(Context.CALLER_DOMAIN));

            ctx.getResponse().setHeader(Context.CAT_SERVER_DOMAIN, Cat.getManager().getDomain());
            ctx.getResponse().setHeader(Context.CAT_SERVER, NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

        } else if (uri.contains("_search")) {
            String name = simpleUrl(uri);
            t = Cat.newTransaction("URL", name);
        } else if (uri.startsWith("/_")) {
            String name = simpleUrl(uri);
            t = Cat.newTransaction("ES", name);
        } else {
            String name = simpleUrl(uri);
            t = Cat.newTransaction("Index", name);
        }
        t.addData(uri + (ctx.getRequest().getQueryString() == null ? "" : "?" + ctx.getRequest().getQueryString()));
        Cat.logRemoteCallServer(ctx);

        return t;
    }

    private String simpleUrl(String url) {
        int idx = simpleUrlIndex(url);
        if (idx > -1) {
            return url.substring(0, idx);
        } else {
            return url;
        }
    }

    private int simpleUrlIndex(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if ('/' == str.charAt(i)) {
                count++;
            }
            if (count == 3) {
                return i;
            }

            if ('?' == str.charAt(i)) {
                return i;
            }


        }

        return -1;
    }


    private static class Context implements Cat.Context {
        private Map<String, String> map = new HashMap<String, String>();

        private static final String ORIGIN_URL = "ORIGIN_URL";
        private static final String CALLER_METHOD = "_catCallerMethod";
        private static final String CALLER_DOMAIN = "_catCallerDomain";
        private static final String REMOTE_IP = "REMOTE_IP";
        private static final String CAT_SERVER_DOMAIN = "_catServerDomain";
        private static final String CAT_SERVER = "_catServer";


        private HttpServletRequest request;
        private HttpServletResponse response;

        public Context(HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;

            addProperty(Cat.Context.ROOT, request.getHeader(Cat.Context.ROOT));
            addProperty(Cat.Context.PARENT, request.getHeader(Cat.Context.PARENT));
            addProperty(Cat.Context.CHILD, request.getHeader(Cat.Context.CHILD));

            addProperty(ORIGIN_URL, request.getRequestURI());
            addProperty(CALLER_DOMAIN, request.getHeader(CALLER_DOMAIN));
            addProperty(CALLER_METHOD, request.getHeader(CALLER_METHOD));

            String remoteIP = request.getRemoteAddr();
            addProperty(REMOTE_IP, remoteIP);
        }


        @Override
        public void addProperty(String key, String value) {
            map.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return map.get(key);
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public HttpServletResponse getResponse() {
            return response;
        }
    }
}
