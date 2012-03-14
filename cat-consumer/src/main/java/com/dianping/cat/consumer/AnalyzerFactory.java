package com.dianping.cat.consumer;

import com.dianping.cat.message.spi.MessageAnalyzer;

/**
 * @author yong.you
 * @since Jan 5, 2012
 */
public interface AnalyzerFactory {

	public MessageAnalyzer create(String name, long start, long duration, long extraTime);

	public void release(Object component);

}