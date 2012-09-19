package com.dianping.cat.report.page.model.logview;

import java.io.IOException;
import java.nio.charset.Charset;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dainping.cat.consumer.dal.report.Logview;
import com.dainping.cat.consumer.dal.report.LogviewDao;
import com.dainping.cat.consumer.dal.report.LogviewEntity;
import com.dianping.cat.hadoop.hdfs.InputChannel;
import com.dianping.cat.hadoop.hdfs.InputChannelManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalLogViewService extends BaseHistoricalModelService<String> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject(value = "html")
	private MessageCodec m_codec;

	@Inject
	private InputChannelManager m_inputChannelManager;

	private Logger m_logger;

	@Inject
	private LogviewDao m_logviewDao;

	public HistoricalLogViewService() {
		super("logview");
	}

	protected String buildModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		Boolean direction = Boolean.valueOf(request.getProperty("direction"));
		String tagThread = request.getProperty("tag");
		MessageTree tree = getLocalLogview(messageId, direction, tagThread);

		// try remote logview
		if (tree == null && !isLocalMode()) {
			tree = getRemoteLogview(messageId, direction, tagThread);
		}

		// if not found, use current message without tag instead
		if (tree == null) {
			tree = getLocalLogview(messageId, null, null);
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

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private MessageTree getLocalLogview(String messageId, Boolean direction, String tagThread) throws IOException {
		MessageId id = MessageId.parse(messageId);
		Bucket<MessageTree> bucket = m_bucketManager.getLogviewBucket(id.getTimestamp(), id.getDomain());
		MessageTree tree = null;

		if (tagThread != null && tagThread.length() > 0) {
			if (direction.booleanValue()) {
				tree = bucket.findNextById(messageId, tagThread);
			} else {
				tree = bucket.findPreviousById(messageId, tagThread);
			}
		} else {
			tree = bucket.findById(messageId);
		}

		return tree;
	}

	private MessageTree getRemoteLogview(String messageId, Boolean direction, String tagThread) throws IOException {
		try {
			Logview logview;

			if (tagThread == null || tagThread.length() == 0) {
				logview = m_logviewDao.findByMessageId(messageId, LogviewEntity.READSET_FULL);
			} else {
				logview = m_logviewDao.findNextByMessageIdTags(messageId, direction.booleanValue(), tagThread, null, null,
				      LogviewEntity.READSET_FULL);
			}

			MessageTree tree = readMessageTree(logview);

			return tree;
		} catch (Exception e) {
			m_logger.error(String.format("Unable to find message(%s, %s, %s)!", messageId, direction, tagThread), e);

			return null;
		}
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

	private MessageTree readMessageTree(Logview logview) throws IOException {
		InputChannel inputChannel = null;

		try {
			String path = logview.getDataPath();
			long offset = logview.getDataOffset();
			int length = logview.getDataLength();

			inputChannel = m_inputChannelManager.openChannel("logview", path);

			MessageTree tree = inputChannel.read(offset, length);

			return tree;
		} finally {
			if (inputChannel != null) {
				m_inputChannelManager.closeChannel(inputChannel);
			}
		}
	}
}
