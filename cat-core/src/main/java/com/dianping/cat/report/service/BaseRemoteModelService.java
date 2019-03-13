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
package com.dianping.cat.report.service;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.server.RemoteServersManager;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public abstract class BaseRemoteModelService<T> extends ModelServiceWithCalSupport implements ModelService<T> {

	private RemoteServersManager m_remoteServersManager;

	private ServerConfigManager m_serverConfigManager;

	private String m_host;

	private String m_name;

	private int m_port = 2281; // default admin port

	@Inject
	private String m_serviceUri = "/cat/r/model";

	public BaseRemoteModelService(String name) {
		m_name = name;
	}

	protected abstract T buildModel(String xml) throws SAXException, IOException;

	public URL buildUrl(ModelRequest request) throws MalformedURLException {
		StringBuilder sb = new StringBuilder(64);

		for (Entry<String, String> e : request.getProperties().entrySet()) {
			if (e.getValue() != null) {
				try {
					sb.append('&');
					sb.append(e.getKey()).append('=').append(URLEncoder.encode(e.getValue(), "utf-8"));
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}
		}
		String url = String.format("http://%s:%s%s/%s/%s/%s?op=xml%s", m_host, m_port, m_serviceUri, m_name,
		      request.getDomain(), request.getPeriod(), sb.toString());

		return new URL(url);
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());

		try {
			URL url = buildUrl(request);

			t.addData(url.toString());

			InputStream in = Urls.forIO().connectTimeout(1000).readTimeout(10000).openStream(url.toExternalForm());
			GZIPInputStream gzip = new GZIPInputStream(in);
			String xml = Files.forIO().readFrom(gzip, "utf-8");

			int len = xml == null ? 0 : xml.length();

			t.addData("length", len);

			if (len > 0) {
				ModelResponse<T> response = new ModelResponse<T>();
				T report = buildModel(xml);

				response.setModel(report);
				t.setStatus(Message.SUCCESS);

				return response;
			} else {
				t.setStatus("NoReport");
			}
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		return null;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		if (m_serverConfigManager.isRemoteServersFixed() && isServersFixed()) {
			Set<String> servers = m_remoteServersManager.queryServers(request.getDomain(), request.getStartTime());
			boolean validate = servers == null || servers.isEmpty() || servers.contains(m_host);

			if (validate) {
				return !period.isHistorical();
			} else {
				return false;
			}
		} else {
			return !period.isHistorical();
		}
	}

	public abstract boolean isServersFixed();

	public void setHost(String host) {
		m_host = host;
	}

	public void setPort(int port) {
		m_port = port;
	}

	public void setRemoteServersManager(RemoteServersManager remoteServersManager) {
		m_remoteServersManager = remoteServersManager;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		m_serverConfigManager = serverConfigManager;
	}

	public void setServiceUri(String serviceUri) {
		m_serviceUri = serviceUri;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name);
		sb.append(']');

		return sb.toString();
	}
}
