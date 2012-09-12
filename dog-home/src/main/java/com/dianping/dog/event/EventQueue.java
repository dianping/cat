package com.dianping.dog.event;


public interface EventQueue<T extends Event> {
		public int size();

		public T poll();

		public boolean offer(T event);
}
