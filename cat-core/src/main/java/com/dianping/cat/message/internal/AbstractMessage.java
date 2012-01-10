package com.dianping.cat.message.internal;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.StringRope;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public abstract class AbstractMessage implements Message {
	private String m_type;

	private String m_name;

	private String m_status;

	private long m_timestamp;

	private StringRope m_data;

	private boolean m_completed;

	public AbstractMessage(String type, String name) {
		m_type = type;
		m_name = name;
		m_timestamp = (long) (System.nanoTime() / 1e6);
		m_data = new StringRope();
	}

	@Override
	public void addData(String keyValuePairs) {
		m_data.addRaw(keyValuePairs);
	}

	@Override
	public void addData(String key, Object value) {
		if (!m_data.isEmpty()) {
			m_data.add("&");
		}

		m_data.add(key).add("=").addRaw(String.valueOf(value));
	}

	@Override
	public StringRope getData() {
		return m_data;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public String getStatus() {
		return m_status;
	}

	@Override
	public long getTimestamp() {
		return m_timestamp;
	}

	@Override
	public String getType() {
		return m_type;
	}

	public boolean isCompleted() {
		return m_completed;
	}

	protected void setCompleted(boolean completed) {
		m_completed = completed;
	}

	@Override
	public boolean isSuccess() {
		return Message.SUCCESS.equals(m_status);
	}

	@Override
	public void setStatus(String status) {
		m_status = status;
	}

	@Override
	public void setStatus(Throwable e) {
		m_status = e.getClass().getName();
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	@Override
	public String toString() {
		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		codec.encodeMessage(this, buf);
		return buf.toString(Charset.forName("utf-8"));
	}
}
