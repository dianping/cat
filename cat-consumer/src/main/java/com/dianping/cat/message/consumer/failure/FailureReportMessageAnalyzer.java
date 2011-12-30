package com.dianping.cat.message.consumer.failure;

import com.dianping.cat.consumer.failurereport.entity.FailureReport;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;

public class FailureReportMessageAnalyzer extends AbstractMessageAnalyzer<FailureReport>{
	
	@Override
	protected void store(FailureReport result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FailureReport generate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void process(MessageTree tree) {
		// TODO Auto-generated method stub
		
	}

}
