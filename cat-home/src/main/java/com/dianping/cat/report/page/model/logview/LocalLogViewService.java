package com.dianping.cat.report.page.model.logview;

import java.nio.charset.Charset;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.TagThreadSupport.Direction;
import com.site.lookup.annotation.Inject;

public class LocalLogViewService implements ModelService<String> {
	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private BucketManager m_bucketManager;

	@Inject(value = "html")
	private MessageCodec m_codec;

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		String messageId = request.getProperty("messageId");
		String direction = request.getProperty("direction");
		String tag = request.getProperty("tag");
		MessageId id = MessageId.parse(messageId);
		String path = m_pathBuilder.getMessagePath(new Date(id.getTimestamp()));
		ModelResponse<String> response = new ModelResponse<String>();

		try {
			Bucket<MessageTree> bucket = m_bucketManager.getMessageBucket(path);
			MessageTree tree = null;

			if (tag != null && direction != null) {
				Direction d = Direction.valueOf(direction);

				tree = bucket.findNextById(messageId, d, tag);
			}

			// if not found, use current instead
			if (tree == null) {
				tree = bucket.findById(messageId);
			}

			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8096);

			m_codec.encode(tree, buf);
			buf.readInt(); // get rid of length
			response.setModel(buf.toString(Charset.forName("utf-8")));
		} catch (Exception e) {
			response.setException(e);
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return !request.getPeriod().isHistorical();
	}
}
