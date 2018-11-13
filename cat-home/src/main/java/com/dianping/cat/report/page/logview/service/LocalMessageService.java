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
package com.dianping.cat.report.page.logview.service;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.message.CodecHandler;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named(type = LocalModelService.class, value = "logview")
public class LocalMessageService extends LocalModelService<String> implements ModelService<String> {
	public static final String ID = DumpAnalyzer.ID;

	@Inject
	private MessageFinderManager m_finderManager;

	@Inject("local")
	private BucketManager m_bucketManager;

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private MessageBucketManager m_messageBucketManager;

	private WaterfallMessageCodec m_waterfall = new WaterfallMessageCodec();

	private HtmlMessageCodec m_html = new HtmlMessageCodec();

	public LocalMessageService() {
		super("logview");
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		String result = buildOldReport(request, period, domain, payload);

		if (result == null) {
			result = buildNewReport(request, period, domain, payload);
		}
		return result;
	}

	private String buildNewReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		String messageId = payload.getMessageId();
		boolean waterfall = payload.isWaterfall();
		MessageId id = MessageId.parse(messageId);
		ByteBuf buf = m_finderManager.find(id);
		MessageTree tree = null;

		try {
			if (buf != null) {
				tree = CodecHandler.decode(buf);
			}

			if (tree == null) {
				Bucket bucket = m_bucketManager
										.getBucket(id.getDomain(),	NetworkInterfaceManager.INSTANCE.getLocalHostAddress(), id.getHour(), false);

				if (bucket != null) {
					bucket.flush();

					ByteBuf data = bucket.get(id);

					if (data != null) {
						tree = CodecHandler.decode(data);
					}
				}
			}
		} finally {
			CodecHandler.reset();
		}

		if (tree != null) {
			ByteBuf content = ByteBufAllocator.DEFAULT.buffer(8192);

			if (tree.getMessage() instanceof Transaction && waterfall) {
				m_waterfall.encode(tree, content);
			} else {
				m_html.encode(tree, content);
			}

			try {
				content.readInt(); // get rid of length
				return content.toString(Charset.forName("utf-8"));
			} catch (Exception e) {
				// ignore it
			}
		}

		return null;
	}

	public String buildOldReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		String messageId = payload.getMessageId();
		boolean waterfall = payload.isWaterfall();
		MessageTree tree = m_messageBucketManager.loadMessage(messageId);

		if (tree != null) {
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

			if (tree.getMessage() instanceof Transaction && waterfall) {
				m_waterfall.encode(tree, buf);
			} else {
				m_html.encode(tree, buf);
			}

			try {
				buf.readInt(); // get rid of length
				return buf.toString(Charset.forName("utf-8"));
			} catch (Exception e) {
				// ignore it
			}
		}
		return null;
	}

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		ModelResponse<String> response = new ModelResponse<String>();
		Transaction t = Cat.newTransaction("ModelService", getClass().getSimpleName());

		try {
			ModelPeriod period = request.getPeriod();
			String domain = request.getDomain();
			ApiPayload payload = new ApiPayload();

			payload.setMessageId(request.getProperty("messageId"));
			payload.setWaterfall(Boolean.valueOf(request.getProperty("waterfall", "false")));

			String report = getReport(request, period, domain, payload);

			response.setModel(report);

			t.addData("period", period);
			t.addData("domain", domain);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Cat.logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}
		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		if (m_configManager.isHdfsOn()) {
			return request.getPeriod().isCurrent();
		} else {
			return true;
		}
	}

}
