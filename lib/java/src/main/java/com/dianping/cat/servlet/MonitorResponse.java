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
