package com.dianping.cat.consumer.demo;

import java.util.Arrays;
import java.util.List;

import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerManager;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer3;

public class ManyAnalyzerMockManager implements MessageAnalyzerManager {
	@Override
	public List<String> getAnalyzerNames() {
		return Arrays.asList("mock1", "mock2", "mock3");
	}

	@Override
	public MessageAnalyzer getAnalyzer(String name, long startTime) {
		if (name.equals("mock1")) {
			MockAnalyzer1 analyzer = new MockAnalyzer1();
			return analyzer;
		} else if (name.equals("mock2")) {
			MockAnalyzer2 analyzer = new MockAnalyzer2();
			return analyzer;
		} else if (name.equals("mock3")) {
			MockAnalyzer3 analyzer = new MockAnalyzer3();
			return analyzer;
		}

		return null;
	}
}
