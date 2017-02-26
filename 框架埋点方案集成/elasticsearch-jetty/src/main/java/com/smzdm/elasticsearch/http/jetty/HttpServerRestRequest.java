/*
 * Copyright 2011 Sonian Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smzdm.elasticsearch.http.jetty;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.support.RestUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengwen.zhu
 */
public class HttpServerRestRequest extends HttpRequest {

    private final HttpServletRequest request;

    private final Method method;

    private final Map<String, String> params;

    private final BytesReference content;
    
    private final String opaqueId;

    public HttpServerRestRequest(HttpServletRequest request) throws IOException {
        this.request = request;
        this.opaqueId = request.getHeader("X-Opaque-Id");
        this.method = Method.valueOf(request.getMethod());
        this.params = new HashMap<String, String>();

        if (request.getQueryString() != null) {
            RestUtils.decodeQueryString(request.getQueryString(), 0, params);
        }

        content = new BytesArray(Streams.copyToByteArray(request.getInputStream()));
    }

    @Override public Method method() {
        return this.method;
    }

    @Override public String uri() {
        int prefixLength = 0;
        if (request.getContextPath() != null ) {
            prefixLength += request.getContextPath().length();
        }
        if (request.getServletPath() != null ) {
            prefixLength += request.getServletPath().length();
        }
        if (prefixLength > 0) {
            return request.getRequestURI().substring(prefixLength);
        } else {
            return request.getRequestURI();
        }
    }

    @Override public String rawPath() {
        return uri();
    }

    @Override public boolean hasContent() {
        return content.length() > 0;
    }

    @Override public boolean contentUnsafe() {
        return false;
    }

    @Override
    public BytesReference content() {
        return content;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override public String header(String name) {
        return request.getHeader(name);
    }

    @Override public Iterable<Map.Entry<String, String>> headers() {
        List<Map.Entry<String, String>> headers = new ArrayList<Map.Entry<String, String>>();
        Enumeration<String> headerNames = this.request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> headerValues = this.request.getHeaders(name);
            while (headerValues.hasMoreElements()) {
                String value = headerValues.nextElement();
                headers.add(new SimpleEntry<String,String>(name, value));
            }
        }
        return headers;
    }

    @Override public Map<String, String> params() {
        return params;
    }

    @Override public boolean hasParam(String key) {
        return params.containsKey(key);
    }

    @Override public String param(String key) {
        return params.get(key);
    }

    @Override public String param(String key, String defaultValue) {
        String value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public String localAddr() {
        return this.request.getLocalAddr();
    }

    public long localPort() {
        return this.request.getLocalPort();
    }

    public String remoteAddr() {
        return this.request.getRemoteAddr();
    }

    public long remotePort() {
        return this.request.getRemotePort();
    }
    
    public String remoteUser() {
        return this.request.getRemoteUser();
    }
    
    public String scheme() {
        return this.request.getScheme();
    }
    
    public String contentType() {
        return this.request.getContentType();
    }

    public String opaqueId() {
        return this.opaqueId;
    }
}
