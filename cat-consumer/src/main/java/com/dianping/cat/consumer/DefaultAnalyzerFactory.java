package com.dianping.cat.consumer;

import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.ip.TopIpAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.site.lookup.ContainerHolder;

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
		} else if (name.equals("dump")) {
			DumpAnalyzer analyzer = lookup(DumpAnalyzer.class);

			analyzer.setAnalyzerInfo(start, duration, extraTime);
			return analyzer;
		} else if (name.equals("ip")) {
			TopIpAnalyzer analyzer = lookup(TopIpAnalyzer.class);

			analyzer.setAnalyzerInfo(start, duration, extraTime);
			return analyzer;
		} else if (name.equals("heartbeat")) {
			HeartbeatAnalyzer analyzer = lookup(HeartbeatAnalyzer.class);

			analyzer.setAnalyzerInfo(start, duration, extraTime);
			return analyzer;
		} 

		throw new RuntimeException(String.format("No analyzer(%s) found!", name));
	}

	@Override
	public void release(Object component) {
		super.release(component);
	}
}
