package com.dianping.cat.hadoop;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DumpToHdfsConsumer implements MessageConsumer {
	public static final String ID = "dump-to-hdfs";

	@Inject
	private MessageStorage m_storage;

	@Inject
	private String m_domain;

	@Override
	public void consume(MessageTree tree) {
		if (m_domain == null || m_domain.equals(tree.getDomain())) {
			m_storage.store(tree);
		}
	}

	@Override
	public String getConsumerId() {
		return ID;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}
}
