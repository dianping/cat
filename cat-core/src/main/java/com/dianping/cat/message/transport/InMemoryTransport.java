package com.dianping.cat.message.transport;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;

public class InMemoryTransport implements Transport {
	private BlockingQueue<Message> m_queue;

	private transient boolean m_active = true;

	public InMemoryTransport() {
		m_queue = new LinkedBlockingQueue<Message>();
	}

	public InMemoryTransport(int queueSize) {
		m_queue = new LinkedBlockingQueue<Message>(queueSize);
	}

	@Override
	public void send(Message message) {
		while (m_active && !m_queue.offer(message)) {
			// throw away the message at the tail
			Message m = m_queue.poll();

			if (m == null) {
				break;
			} else {
				System.out.println(message + " was thrown away due to queue is full!");
			}
		}
	}

	@Override
	public void onMessage(MessageHandler handler) {
		try {
			while (true) {
				Message m = m_queue.poll(1L, TimeUnit.MILLISECONDS);

				if (m != null) {
					handler.handle(m);
				} else if (!m_active) {
					break;
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	@Override
	public void shutdown() {
		m_active = false;
	}
}
