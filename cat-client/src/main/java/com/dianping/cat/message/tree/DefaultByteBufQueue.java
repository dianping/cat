package com.dianping.cat.message.tree;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureProperty;

import io.netty.buffer.ByteBuf;

// Component
public class DefaultByteBufQueue implements ByteBufQueue, Initializable {
	private BlockingQueue<ByteBuf> m_queue;

	@Override
	public boolean offer(ByteBuf buf) {
		return m_queue.offer(buf);
	}

	@Override
	public ByteBuf poll() {
		try {
			return m_queue.poll(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		ConfigureManager configureManager = ctx.lookup(ConfigureManager.class);
		int size = configureManager.getIntProperty(ConfigureProperty.SENDER_MESSAGE_QUEUE_SIZE, 5000);

		m_queue = new ArrayBlockingQueue<ByteBuf>(size);
	}
}
