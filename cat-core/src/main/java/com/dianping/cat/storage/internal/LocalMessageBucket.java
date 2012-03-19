package com.dianping.cat.storage.internal;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.annotation.Inject;

public class LocalMessageBucket extends AbstractFileBucket<MessageTree> {
	@Inject
	private MessageCodec m_codec;

	@Override
	protected MessageTree decode(ChannelBuffer buf) throws IOException {
		MessageTree tree = new DefaultMessageTree();

		m_codec.decode(buf, tree);
		return tree;
	}

	@Override
	protected void encode(MessageTree tree, ChannelBuffer buf) throws IOException {
		m_codec.encode(tree, buf);
	}

	@Override
	protected boolean isAutoFlush() {
		return true;
	}

	@Override
	public boolean storeById(String id, MessageTree tree) {
		String tagThread = "t:" + tree.getThreadId();
		String tagSession = "s:" + tree.getSessionToken();
		String tagRequest = "r:" + tree.getMessageId();

		return storeById(id, tree, new String[] { tagThread, tagSession, tagRequest });
	}

	public void setCodec(MessageCodec codec) {
		m_codec = codec;
	}
}
