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
package com.dianping.cat.report;

import java.util.Map;

public interface ReportDelegate<T> {
	public void afterLoad(Map<String, T> reports);

	public void beforeSave(Map<String, T> reports);

	public byte[] buildBinary(T report);

	public T parseBinary(byte[] bytes);

	public String buildXml(T report);

	public String getDomain(T report);

	public T makeReport(String domain, long startTime, long duration);

	public T mergeReport(T old, T other);

	public T parseXml(String xml) throws Exception;

	public boolean createHourlyTask(T report);

}