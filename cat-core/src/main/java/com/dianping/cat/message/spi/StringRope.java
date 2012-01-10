package com.dianping.cat.message.spi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.spi.codec.BufferWriter;

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
		return addObject(str, false);
	}

	public StringRope add(StringRope rope) {
		int size = rope.size();

		for (int i = 0; i < size; i++) {
			addObject(rope.m_parts.get(i), rope.m_flags.get(i));
		}

		return this;
	}

	protected StringRope addObject(Object obj, boolean isRaw) {
		m_flags.set(m_parts.size(), isRaw);
		m_parts.add(String.valueOf(obj));
		return this;
	}

	public StringRope addRaw(String str) {
		return addObject(str, true);
	}

	public boolean isEmpty() {
		return m_parts.isEmpty();
	}

	public int size() {
		return m_parts.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);

		for (String part : m_parts) {
			sb.append(part);
		}

		return sb.toString();
	}

	public int writeTo(ChannelBuffer buffer, BufferWriter writer) {
		int size = m_parts.size();
		int count = 0;

		for (int i = 0; i < size; i++) {
			String part = m_parts.get(i);
			byte[] data;

			if (!m_flags.get(i)) { // no need to escape
				data = part.getBytes();
			} else {
				try {
					data = part.getBytes("utf-8");
				} catch (UnsupportedEncodingException e) {
					data = part.getBytes();
				}
			}

			count += writer.writeTo(buffer, data);
		}

		return count;
	}
}
