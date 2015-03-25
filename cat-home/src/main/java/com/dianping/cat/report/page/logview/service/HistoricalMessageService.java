package com.dianping.cat.report.page.logview.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalMessageService  extends BaseHistoricalModelService<String> {
	@Inject(HdfsMessageBucketManager.ID)
	private MessageBucketManager m_hdfsBucketManager;

	@Inject(HtmlMessageCodec.ID)
	private MessageCodec m_html;

	@Inject(WaterfallMessageCodec.ID)
	private MessageCodec m_waterfall;

	public HistoricalMessageService() {
		super("logview");
	}

	@Override
   protected String buildModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		Cat.logEvent("LoadMessage", "messageTree", Event.SUCCESS, messageId);
		MessageTree tree = m_hdfsBucketManager.loadMessage(messageId);

		if (tree != null) {
			return toString(request, tree);
		} else {
			return null;
		}
   }

	@Override
	public boolean isEligable(ModelRequest request) {
		boolean eligibale = request.getPeriod().isHistorical();

		if (eligibale) {
			String messageId = request.getProperty("messageId");
			MessageId id = MessageId.parse(messageId);

			return id.getVersion() == 2;
		}

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
