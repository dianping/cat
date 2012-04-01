package com.dianping.cat.consumer;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.site.lookup.ContainerHolder;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public class DefaultAnalyzerFactory extends ContainerHolder implements AnalyzerFactory {
	@Override
	public MessageAnalyzer create(String name, long start, long duration, long extraTime) {
		if (name.equals("problem")) {
			ProblemAnalyzer analyzer = lookup(ProblemAnalyzer.class);

			analyzer.setAnalyzerInfo(start, duration, extraTime);
			return analyzer;
		} else if (name.equals("transaction")) {
			TransactionAnalyzer analyzer = lookup(TransactionAnalyzer.class);

			analyzer.setAnalyzerInfo(start, duration, extraTime);
			return analyzer;
		} else if (name.equals("event")) {
			EventAnalyzer analyzer = lookup(EventAnalyzer.class);

			analyzer.setAnalyzerInfo(start, duration, extraTime);
			return analyzer;
		} else if (name.equals("ip")) {
			IpAnalyzer analyzer = lookup(IpAnalyzer.class);

			return analyzer;
		}

		throw new RuntimeException(String.format("No analyzer(%s) found!", name));
	}

	@Override
	public void release(Object component) {
		super.release(component);
	}
}
