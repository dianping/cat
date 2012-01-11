package com.dianping.cat.consumer.impl;

import com.dianping.cat.consumer.impl.AnalyzerFactory;
import com.dianping.cat.consumer.impl.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.impl.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.impl.ManyAnalyzerTest.MockAnalyzer3;
import com.dianping.cat.message.spi.MessageAnalyzer;

public class ManyAnalyerMockFactory implements AnalyzerFactory{

	public MessageAnalyzer create(String name, long start, long duration ,String domain ,long extraTime) {
		if (name.equals("mock1")) {
			MockAnalyzer1 analyzer = new MockAnalyzer1();			
			return analyzer;
		} else if(name.equals("mock2")) {
			MockAnalyzer2 analyzer = new MockAnalyzer2();			
			return analyzer;
		} else if (name.equals("mock3")) {
			MockAnalyzer3 analyzer = new MockAnalyzer3();			
			return analyzer;
		}
		return null;
	}

	@Override
	public void release(Object component) {
		
	}
}
