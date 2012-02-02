package com.dianping.cat.consumer.ip;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;

public class IpAnalyzer extends AbstractMessageAnalyzer<IpReport> {
	@Override
	protected void store(IpReport result) {
		// TODO Auto-generated method stub
	}

	@Override
	public IpReport generate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void process(MessageTree tree) {

	}

	@Override
	protected boolean isTimeout() {
		return false;
	}
}
