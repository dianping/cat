package com.dianping.cat.consumer.impl;

import com.dianping.cat.message.spi.MessageAnalyzer;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public interface AnalyzerFactory {

	public MessageAnalyzer create(String name, long start, long duration,
			String domain, long extraTime);

	public void release(Object component);

}