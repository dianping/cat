package com.dianping.dog.alarm.merge;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventQueue;
import com.site.lookup.annotation.Inject;


public class DefaultEventQueue <T extends Event>implements EventQueue<T>{
	private BlockingQueue<T> m_queue;

	@Inject
	private int m_size;

	public DefaultEventQueue(){
		if (m_size > 0) {
			m_queue = new LinkedBlockingQueue<T>(m_size);
		} else {
			m_queue = new LinkedBlockingQueue<T>(10000);
		}
	}

	@Override
	public int size() {
		return m_queue.size();
	}

	public void setSize(int size) {
		m_size = size;
	}

	@Override
   public boolean offer(T event) {
		return m_queue.offer(event);
   }

	@Override
   public T poll() {
		try {
			return m_queue.poll(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}
   }
	
}



