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
package com.dianping.cat.system.page.plugin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.system.SystemPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	// TODO make it configurable in database
	private Map<String, String> m_serverMapping = new LinkedHashMap<String, String>();

	public Handler() {
		// Production
		m_serverMapping.put("10.1.6.37:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.8.64:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.102:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.108:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.126:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.128:8080", "cat.dianpingoa.com");
		m_serverMapping.put("10.1.6.145:8080", "cat.dianpingoa.com");

		// QATE
		m_serverMapping.put("192.168.7.70:8080", "cat.qa.dianpingoa.com");
	}

	private void addResourceFiles(ZipOutputStream zos, String baseDir, String... paths) throws IOException {
		for (String path : paths) {
			ZipEntry entry = new ZipEntry(path);
			String resource = baseDir + "/" + path;
			byte[] data = Files.forIO().readFrom(getClass().getResourceAsStream(resource));

			zos.putNextEntry(entry);
			zos.write(data);
		}
	}

	private void downloadChromeExtension(Context ctx) throws IOException {
		Payload payload = ctx.getPayload();
		HttpServletResponse res = ctx.getHttpServletResponse();

		if (payload.isDownloadMapping()) {
			StringBuilder sb = new StringBuilder(1024);
			boolean first = true;

			sb.append('{');

			for (Map.Entry<String, String> e : m_serverMapping.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}

				sb.append('"').append(e.getKey()).append("\":\"").append(e.getValue()).append('"');
			}

			sb.append('}');

			byte[] content = sb.toString().getBytes("utf-8");

			res.setContentType("application/json; charset=utf-8");
			res.setContentLength(content.length);
			res.getOutputStream().write(content);
		} else if (payload.isDownloadSource()) {
			ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
			res.setContentType("application/x-zip-compressed");
			res.addHeader("Content-Disposition", "attachment;filename=cat.zip");
			addResourceFiles(zos, "/chrome/cat", "manifest.json", "cat.png", "cat.js");
			zos.close();
		} else {
			InputStream is = getClass().getResourceAsStream("/chrome/cat.crx");

			// res.setContentType("application/x-chrome-extension"); // chrome disabled inline install
			res.setContentType("application/octet-stream");
			res.addHeader("Content-Disposition", "attachment;filename=cat.crx");

			Files.forIO().copy(is, res.getOutputStream(), AutoClose.INPUT);
		}

		ctx.stopProcess();
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "plugin")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "plugin")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		String type = payload.getType();

		model.setPage(SystemPage.PLUGIN);
		Action action = payload.getAction();
		model.setAction(action);

		switch (action) {
		case VIEW:
			if ("chrome".equals(type)) {
				downloadChromeExtension(ctx);
			}
			break;
		case DOCFILE:
			downloadFile(ctx);
			break;
		}
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void downloadFile(Context ctx) throws IOException {
		Payload payload = ctx.getPayload();
		HttpServletResponse res = ctx.getHttpServletResponse();
		String file = payload.getFile();

		if (StringUtils.isNotBlank(file)) {
			InputStream is = getClass().getResourceAsStream("/doc/" + file);

			res.setContentType("application/octet-stream");
			res.addHeader("Content-Disposition", "attachment;filename=" + file);

			Files.forIO().copy(is, res.getOutputStream(), AutoClose.INPUT);
		}

		ctx.stopProcess();
	}
}
