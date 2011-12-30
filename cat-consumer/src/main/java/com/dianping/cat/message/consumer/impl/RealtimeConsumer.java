package com.dianping.cat.message.consumer.impl;

import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;

public class RealtimeConsumer implements MessageConsumer{
	public List<RealtimeTask> tasks;
	@Override
	public String getConsumerId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void consume(MessageTree tree) {
		for(RealtimeTask task:tasks){
			task.consume(tree);
		}		
	}
}
