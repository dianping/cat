/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.servlet;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.status.http.HttpStats;
import com.dianping.cat.util.Joiners;
import com.dianping.cat.util.UrlParser;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultTransaction;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CatFilter implements Filter {
    private String servers;
    private Set<String> excludeUrls;
    private Set<String> excludePrefixes;

    private void customizeStatus(Transaction t, HttpServletRequest req) {
        Object catStatus = req.getAttribute(CatConstants.CAT_STATE);

        if (catStatus != null) {
            t.setStatus(catStatus.toString());
        } else {
            t.setStatus(Message.SUCCESS);
        }
    }

    private void customizeUri(Transaction t, HttpServletRequest req) {
        if (t instanceof DefaultTransaction) {
            Object catPageType = req.getAttribute(CatConstants.CAT_PAGE_TYPE);

            if (catPageType instanceof String) {
                ((DefaultTransaction) t).setType(catPageType.toString());
            }

            Object catPageUri = req.getAttribute(CatConstants.CAT_PAGE_URI);

            if (catPageUri instanceof String) {
                ((DefaultTransaction) t).setName(catPageUri.toString());
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();
        boolean exclude = excludePath(path);

        if (exclude) {
            chain.doFilter(request, response);
            return;
        }

        logTransaction(chain, req, res);
    }

    private boolean excludePath(String path) {
        try {
            boolean exclude = excludeUrls != null && excludeUrls.contains(path);

            if (!exclude && excludePrefixes != null) {
                for (String prefix : excludePrefixes) {
                    if (path.startsWith(prefix)) {
                        exclude = true;
                        break;
                    }
                }
            }
            return exclude;
        } catch (Exception e) {
            return false;
        }
    }

    private String getCatServer() {
        try {
            if (servers == null) {
                DefaultMessageManager manager = (DefaultMessageManager) Cat.getManager();
                List<Server> servers = manager.getConfigService().getServers();

                this.servers = Joiners.by(',').join(servers, new Joiners.IBuilder<Server>() {
                    @Override
                    public String asString(Server server) {
                        String ip = server.getIp();
                        int httpPort = server.getHttpPort();

                        return ip + ":" + httpPort;
                    }
                });
            }

            return servers;
        } catch (Exception e) {
            return null;
        }
    }

    private String getRequestURI(HttpServletRequest req) {
        return UrlParser.format(req.getRequestURI());
    }

    @Override
    public void init(FilterConfig filterConfig) {
        String exclude = filterConfig.getInitParameter("exclude");

        if (exclude != null) {
            excludeUrls = new HashSet<String>();
            String[] excludeUrls = exclude.split(";");

            for (String s : excludeUrls) {
                int index = s.indexOf("*");

                if (index > 0) {
                    if (excludePrefixes == null) {
                        excludePrefixes = new HashSet<String>();
                    }
                    excludePrefixes.add(s.substring(0, index));
                } else {
                    this.excludeUrls.add(s);
                }
            }
        }
    }

    private void logCatMessageId(HttpServletResponse res) {
        boolean isTraceMode = Cat.getManager().isTraceMode();

        if (isTraceMode) {
            String id = Cat.getCurrentMessageId();

            res.setHeader("X-CAT-ROOT-ID", id);
            res.setHeader("X-CAT-SERVER", getCatServer());
        }
    }

    private void logPayload(HttpServletRequest req, boolean top, String type) {
        try {
            if (top) {
                logRequestClientInfo(req, type);
                logRequestPayload(req, type);
            } else {
                logRequestPayload(req, type);
            }
        } catch (Exception e) {
            Cat.logError(e);
        }
    }

    private void logRequestClientInfo(HttpServletRequest req, String type) {
        StringBuilder sb = new StringBuilder(1024);
        String ip = "";
        String ipForwarded = req.getHeader("x-forwarded-for");

        if (ipForwarded == null) {
            ip = req.getRemoteAddr();
        } else {
            ip = ipForwarded;
        }

        sb.append("IPS=").append(ip);
        sb.append("&VirtualIP=").append(req.getRemoteAddr());
        sb.append("&Server=").append(req.getServerName());
        sb.append("&Referer=").append(req.getHeader("referer"));
        sb.append("&Agent=").append(req.getHeader("user-agent"));

        Cat.logEvent(type, type + ".Server", Message.SUCCESS, sb.toString());
    }

    private void logRequestPayload(HttpServletRequest req, String type) {
        StringBuilder sb = new StringBuilder(256);

        sb.append(req.getScheme().toUpperCase()).append('/');
        sb.append(req.getMethod()).append(' ').append(req.getRequestURI());

        String qs = req.getQueryString();

        if (qs != null) {
            sb.append('?').append(qs);
        }

        Cat.logEvent(type, type + ".Method", Message.SUCCESS, sb.toString());
    }

    private void logTraceMode(HttpServletRequest req) {
        String traceMode = "X-CAT-TRACE-MODE";
        String headMode = req.getHeader(traceMode);

        if ("true".equals(headMode)) {
            Cat.getManager().setTraceMode(true);
        }
    }

    private void logTransaction(FilterChain chain, HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {
        Message message = Cat.getManager().getThreadLocalMessageTree().getMessage();
        boolean top = message == null;
        String type;

        MonitorResponse res = new MonitorResponse(response);
        long start = System.currentTimeMillis();
        HttpStats stats = HttpStats.currentStatsHolder();
        int status = 0;

        if (top) {
            type = CatConstants.TYPE_URL;
            logTraceMode(req);
        } else {
            type = CatConstants.TYPE_URL_FORWARD;
        }

        Transaction t = Cat.newTransaction(type, getRequestURI(req));

        try {
            logPayload(req, top, type);
            logCatMessageId(res);
            chain.doFilter(req, res);
            customizeStatus(t, req);
        } catch (ServletException e) {
            status = 500;
            t.setStatus(e);
            Cat.logError(e);
            throw e;
        } catch (IOException e) {
        	status = 500;
            t.setStatus(e);
            Cat.logError(e);
            throw e;
        } catch (Throwable e) {
            status = 500;
            t.setStatus(e);
            Cat.logError(e);
            throw new RuntimeException(e);
        } finally {
            customizeUri(t, req);
            t.complete();

            long end = System.currentTimeMillis();
            stats.doRequestStats(end - start, status > 0 ? status : res.getCurrentStatus());
        }
    }

}
