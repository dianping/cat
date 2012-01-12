package com.dianping.cat.consumer;

import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMessageAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.site.lookup.ContainerHolder;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public class DefaultAnalyzerFactory extends ContainerHolder implements
		AnalyzerFactory {

	@Override
	public MessageAnalyzer create(String name, long start, long duration,
			String domain, long extraTime) {
		if (name.equals("failure")) {
			FailureReportAnalyzer analyzer = lookup(FailureReportAnalyzer.class);
			
			analyzer.setAnalyzerInfo(start, duration, domain, extraTime);
			return analyzer;
		} else if (name.equals("transaction")) {
			TransactionReportMessageAnalyzer analyzer= lookup(TransactionReportMessageAnalyzer.class);
			analyzer.setAnalyzerInfo(start, duration, domain, extraTime);
			return analyzer;
		}

		return null;
	}

	@Override
	public void release(Object component) {
		release(component);
	}
}
