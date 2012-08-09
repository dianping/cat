package com.dianping.cat.report.page.model.logview;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.HtmlMessageCodec;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalMessageService extends BaseLocalModelService<String> {
	@Inject(LocalMessageBucketManager.ID)
	private MessageBucketManager m_localBucketManager;

	@Inject(HdfsMessageBucketManager.ID)
	private MessageBucketManager m_hdfsBucketManager;

	@Inject(HtmlMessageCodec.ID)
	private MessageCodec m_codec;

	public HistoricalMessageService() {
		super("logview");
	}

	@Override
	protected String getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		String messageId = request.getProperty("messageId");

		if (messageId == null) {
			return null;
		}

		MessageTree tree = m_localBucketManager.loadMessage(messageId);

		if (tree != null) {
			return toString(tree);
		}

		tree = m_hdfsBucketManager.loadMessage(messageId);

		if (tree != null) {
			return toString(tree);
		} else {
			return null;
		}
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		boolean eligibale = !request.getPeriod().isCurrent();

		if (eligibale) {
			String messageId = request.getProperty("messageId");
			MessageId id = MessageId.parse(messageId);

			return id.getVersion() == 2;
		}

		return eligibale;
	}

	private String toString(MessageTree tree) {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_codec.encode(tree, buf);
		buf.readInt(); // get rid of length
		return buf.toString(Charset.forName("utf-8"));
	}
}
