package com.dianping.cat.report.page.model.logview;

import java.nio.charset.Charset;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HdfsLogViewService implements ModelService<String> {
	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Inject
	private BucketManager m_bucketManager;

	@Inject(value = "plain-text")
	private MessageCodec m_plainDecode;

	@Inject(value = "html")
	private MessageCodec m_htmlCodec;

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		String messageId = request.getProperty("messageId");
		String direction = request.getProperty("direction");
		String tag = request.getProperty("tag");
		MessageId id = MessageId.parse(messageId);
		String path = m_pathBuilder.getMessagePath(id.getDomain(), new Date(id.getTimestamp()));
		ModelResponse<String> response = new ModelResponse<String>();

		try {
			Bucket<byte[]> bucket = m_bucketManager.getHdfsBucket(path);
			byte[] data = null;

			if (tag != null && direction != null) {
				Boolean d = Boolean.valueOf(direction);

				if (d.booleanValue()) {
					data = bucket.findNextById(messageId, tag);
				} else {
					data = bucket.findPreviousById(messageId, tag);
				}
			}

			// if not found, use current instead
			if (data == null) {
				data = bucket.findById(messageId);
			}

			if (data != null) {
				ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
				MessageTree tree = new DefaultMessageTree();

				buf.writeBytes(data);
				m_plainDecode.decode(buf, tree);
				buf.resetReaderIndex();
				buf.resetWriterIndex();
				m_htmlCodec.encode(tree, buf);
				buf.readInt(); // get rid of length
				response.setModel(buf.toString(Charset.forName("utf-8")));
			}
		} catch (Exception e) {
			response.setException(e);
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
	}
}
