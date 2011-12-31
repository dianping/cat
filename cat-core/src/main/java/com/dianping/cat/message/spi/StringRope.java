package com.dianping.cat.message.spi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

public class StringRope {
	private List<String> m_parts;

	private BitSet m_flags;

	public StringRope() {
		this(10);
	}

	public StringRope(int initialSize) {
		m_parts = new ArrayList<String>(initialSize);
		m_flags = new BitSet(1024);
	}

	public StringRope add(String str) {
		return add(str, false);
	}

	public StringRope add(String str, boolean utf8) {
		return add(str, utf8);
	}

	protected StringRope add(Object obj, boolean utf8) {
		m_flags.set(m_parts.size(), utf8);
		m_parts.add(String.valueOf(obj));
		return this;
	}

	public int size() {
		return m_parts.size();
	}

	public boolean isEmpty() {
		return m_parts.isEmpty();
	}

	public void writeTo(ChannelBuffer buffer) {
		int size = m_parts.size();
		int writeIndex = buffer.writerIndex();
		int total = 0;

		buffer.writeInt(0); // place-holder

		for (int i = 0; i < size; i++) {
			String part = m_parts.get(i);
			byte[] data;

			if (!m_flags.get(i)) { // no need to encode
				data = part.getBytes();
			} else {
				try {
					data = part.getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					data = part.getBytes();
				}
			}

			buffer.writeBytes(data);
			total += data.length;
		}

		buffer.setInt(writeIndex, total);
	}

	public StringRope add(StringRope rope) {
		int size = rope.size();

		for (int i = 0; i < size; i++) {
			add(rope.m_parts.get(i), rope.m_flags.get(i));
		}

		return this;
	}
}
