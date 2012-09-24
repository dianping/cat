package com.dianping.cat.report.page.model.logview;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalLogViewService extends BaseLocalModelService<String> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject(value = "html")
	private MessageCodec m_codec;

	public LocalLogViewService() {
		super("logview");
	}

	private MessageTree getLogviewFromBucket(Bucket<MessageTree> bucket, String messageId, String direction, String tag)
	      throws IOException {
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
		return tree;
	}

	@Override
	protected String getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		String messageId = request.getProperty("messageId");

		if (messageId == null) {
			return null;
		}

		String direction = request.getProperty("direction");
		String tag = request.getProperty("tag");
		MessageId id = MessageId.parse(messageId);
		Bucket<MessageTree> bucket = m_bucketManager.getLogviewBucket(id.getTimestamp(), id.getDomain());
		MessageTree tree = null;

		tree = getLogviewFromBucket(bucket, messageId, direction, tag);

		if (tree == null) {
			List<Bucket<MessageTree>> buckets = m_bucketManager.getLogviewBuckets(id.getTimestamp(), id.getDomain());
			for (Bucket<MessageTree> b : buckets) {
				tree = getLogviewFromBucket(b, messageId, direction, tag);
				if (tree != null) {
					break;
				}
			}
		}

		if (tree != null) {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

			if (tree.getMessage() instanceof Transaction && request.getProperty("waterfall", "false").equals("true")) {
				// to work around a plexus injection bug
				MessageCodec codec = lookup(MessageCodec.class, "waterfall");

				codec.encode(tree, buf);
			} else {
				m_codec.encode(tree, buf);
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
		boolean eligibale = super.isEligable(request);

		if (eligibale) {
			String messageId = request.getProperty("messageId");
			MessageId id = MessageId.parse(messageId);

			return id.getVersion() == 1;
		}

		return eligibale;
	}
}
