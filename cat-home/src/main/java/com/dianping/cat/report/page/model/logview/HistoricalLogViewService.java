package com.dianping.cat.report.page.model.logview;

import java.nio.charset.Charset;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalLogViewService extends BaseHistoricalModelService<String> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject(value = "html")
	private MessageCodec m_codec;

	public HistoricalLogViewService() {
		super("logview");
	}

	protected String buildModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		String direction = request.getProperty("direction");
		String tag = request.getProperty("tag");
		MessageId id = MessageId.parse(messageId);
		Date timestamp = new Date(id.getTimestamp());
		Bucket<MessageTree> bucket = m_bucketManager.getMessageBucket(timestamp, id.getDomain(), "remote");
		MessageTree tree = null;

		if (tag != null && direction != null) {
			Boolean d = Boolean.valueOf(direction);

			if (d.booleanValue()) {
				tree = bucket.findNextById(messageId, tag);
			} else {
				tree = bucket.findPreviousById(messageId, tag);
			}
		}

		// if not found, use current instead
		if (tree == null) {
			tree = bucket.findById(messageId);
		}

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
