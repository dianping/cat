package com.dianping.cat.report.page.model.logview;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.lookup.annotation.Inject;

public class LocalMessageService extends BaseLocalModelService<String> {
	@Inject
	private MessageBucketManager m_bucketManager;

	@Inject(value = "html")
	private MessageCodec m_codec;

	public LocalMessageService() {
		super("logview");
	}

	@Override
	protected String getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		String messageId = request.getProperty("messageId");

		if (messageId == null) {
			return null;
		}

		MessageTree tree = m_bucketManager.loadMessage(messageId);

		if (tree != null) {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

			m_codec.encode(tree, buf);
			buf.readInt(); // get rid of length
			return buf.toString(Charset.forName("utf-8"));
		} else {
			return null;
		}
	}
}
