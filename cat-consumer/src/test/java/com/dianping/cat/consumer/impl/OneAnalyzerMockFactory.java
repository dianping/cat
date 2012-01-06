package com.dianping.cat.consumer.impl;

import com.dianping.cat.consumer.impl.OneAnalyzerTwoDurationTest.MockAnalyzer;
import com.dianping.cat.message.consumer.impl.AnalyzerFactory;
import com.dianping.cat.message.spi.MessageAnalyzer;

public class OneAnalyzerMockFactory implements AnalyzerFactory{
	public MessageAnalyzer create(String name, long start, long duration ,String domain ,long extraTime) {
		if (name.equals("mock")) {
			MockAnalyzer analyzer = new OneAnalyzerTwoDurationTest.MockAnalyzer();			
			return analyzer;
		} 
		return null;
	}

	@Override
	public void release(Object component) {
		
	}
}
