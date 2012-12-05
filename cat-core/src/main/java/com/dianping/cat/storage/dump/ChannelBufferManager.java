package com.dianping.cat.storage.dump;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class ChannelBufferManager {
	private BlockingQueue<ChannelBuffer> m_pool = new LinkedBlockingQueue<ChannelBuffer>(100);

	public ChannelBuffer allocate() {
		ChannelBuffer buffer = m_pool.poll();

		if (buffer != null) {
			return buffer;
		} else {
			return ChannelBuffers.dynamicBuffer(16384);
		}
	}

	public void revoke(ChannelBuffer buffer) {
		if (buffer.capacity() <= 16384) { // get rid of big buffer
			buffer.clear();
			m_pool.offer(buffer);
		}
	}
}
