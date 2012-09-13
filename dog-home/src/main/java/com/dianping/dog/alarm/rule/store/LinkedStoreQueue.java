package com.dianping.dog.alarm.rule.store;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

public class LinkedStoreQueue<T extends Data> extends ReentrantLock implements StoreQueue<T> {

	private static final long serialVersionUID = 1L;

	private volatile Entry head;

	private volatile Entry tail;

	private volatile int size;

	private volatile int curSize;

	@SuppressWarnings("unused")
   private LinkedStoreQueue() {
	}

	public LinkedStoreQueue(int size) {
		this.size = size;
		this.curSize = 0;
	}

	@Override
	public void addData(T data) {
		lock();
		try {
			curSize++;
			if (curSize == size) {
				removeLast();
			}
			if (head == null) {
				init(data);
			} else {
				Entry oldHead = head;
				Entry newHead = new Entry(null, oldHead, data);
				oldHead.prev = newHead;
				head = newHead;
			}
		} finally {
			unlock();
		}
	}

	private void init(T data) {
		Entry ctx = new Entry(null, null, data);
		head = tail = ctx;
	}

	private T removeLast() {
		Entry oldTail = tail;
		if (oldTail == null) {
			throw new NoSuchElementException();
		}

		if (oldTail.prev == null) {
			head = tail = null;
		} else {
			oldTail.prev.next = null;
			tail = oldTail.prev;
		}

		return oldTail.getData();
	}

	@Override
	public List<T> getAll() {
		lock();
		List<T> dataList = new ArrayList<T>();
		try {
			if (head == null) {
				throw new NullPointerException("data");
			}

			Entry ctx = head;
			for (;;) {
				dataList.add(ctx.getData());
				ctx = ctx.next;
				if (ctx == null) {
					break;
				}
			}
		} finally {
			unlock();
		}
		return dataList;
	}

	private final class Entry {
		volatile Entry next;

		volatile Entry prev;

		private final T data;

		Entry(Entry prev, Entry next, T data) {

			if (data == null) {
				throw new NullPointerException("data");
			}

			this.prev = prev;
			this.next = next;
			this.data = data;
		}

		T getData() {
			return this.data;
		}

	}

}
