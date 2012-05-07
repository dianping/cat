package com.dianping.cat.message.internal;

import java.util.Collections;
import java.util.List;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public enum NullMessage implements Transaction, Event, Heartbeat {
	TRANSACTION,

	EVENT,

	HEARTBEAT;

	@Override
	public void addData(String keyValuePairs) {
	}

	@Override
	public void addData(String key, Object value) {
	}

	@Override
	public void complete() {
	}

	@Override
	public Object getData() {
		return null;
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getStatus() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public void setStatus(String status) {
	}

	@Override
	public void setStatus(Throwable e) {
	}

	@Override
	public Transaction addChild(Message message) {
		return this;
	}

	@Override
	public List<Message> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public long getDurationInMillis() {
		return 0;
	}

	@Override
	public long getDurationInMicros() {
		return 0;
	}

	@Override
	public boolean isStandalone() {
		return true;
	}
}
