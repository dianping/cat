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
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalMessageService extends BaseHistoricalModelService<String> {

	@Inject
	private HdfsBucketManager m_bucketManager;

	@Inject(type = MessageBucketManager.class, value = HdfsMessageBucketManager.ID)
	private MessageBucketManager m_hdfsBucketManager;

	private WaterfallMessageCodec m_waterfall = new WaterfallMessageCodec();

	private HtmlMessageCodec m_html = new HtmlMessageCodec();

	public HistoricalMessageService() {
		super("logview");
	}

	@Override
	protected String buildModel(ModelRequest request) throws Exception {
		String result = buildNewMessageModel(request);

		if (result == null) {
			result = buildOldMessageModel(request);
		}
		return result;
	}

	protected String buildOldMessageModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		Cat.logEvent("LoadMessage", "messageTree", Event.SUCCESS, messageId);
		MessageTree tree = m_hdfsBucketManager.loadMessage(messageId);

		if (tree != null) {
			return toString(request, tree);
		} else {
			return null;
		}
	}

	protected String buildNewMessageModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		Cat.logEvent("LoadMessage", "messageTree", Event.SUCCESS, messageId);
		MessageId id = MessageId.parse(messageId);
		MessageTree tree = m_bucketManager.loadMessage(id);

		if (tree != null) {
			return toString(request, tree);
		} else {
			return null;
		}
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		boolean eligibale = request.getPeriod().isHistorical();

		return eligibale;
	}

	protected String toString(ModelRequest request, MessageTree tree) {
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

		if (tree.getMessage() instanceof Transaction && request.getProperty("waterfall", "false").equals("true")) {
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
		return null;
	}
}
