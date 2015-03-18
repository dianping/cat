package com.dianping.cat.report.page.logview.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.BasePayload;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.core.HtmlMessageCodec;
import com.dianping.cat.message.spi.core.WaterfallMessageCodec;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.message.MessageBucketManager;

public class LocalMessageService extends LocalModelService<String> {

	public static final String ID = DumpAnalyzer.ID;

	@Inject(LocalMessageBucketManager.ID)
	private MessageBucketManager m_bucketManager;

	@Inject(HtmlMessageCodec.ID)
	private MessageCodec m_html;

	@Inject(WaterfallMessageCodec.ID)
	private MessageCodec m_waterfall;

	public LocalMessageService() {
		super("logview");
	}

	@Override
	public String getReport(ModelRequest request, ModelPeriod period, String domain, BasePayload payload)
	      throws Exception {
		String messageId = payload.getMessageId();
		boolean waterfull = payload.isWaterfall();
		MessageTree tree = m_bucketManager.loadMessage(messageId);

		if (tree != null) {
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

			if (tree.getMessage() instanceof Transaction && waterfull) {
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
	public boolean isEligable(ModelRequest request) {
		if (m_manager.isHdfsOn()) {
			boolean eligibale = request.getPeriod().isCurrent();

			if (eligibale) {
				String messageId = request.getProperty("messageId");
				MessageId id = MessageId.parse(messageId);

				return id.getVersion() == 2;
			}

			return eligibale;
		} else {
			return true;
		}
	}

}
