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

	private int writeRaw(ChannelBuffer buffer, byte[] data) {
		int len = data.length;
		int count = len;
		int offset = 0;

		for (int i = 0; i < len; i++) {
			byte b = data[i];

			if (b == '\t' || b == '\r' || b == '\n' || b == '\\') {
				buffer.writeBytes(data, offset, i - offset);
				buffer.writeByte('\\');

				if (b == '\t') {
					buffer.writeByte('t');
				} else if (b == '\r') {
					buffer.writeByte('r');
				} else if (b == '\n') {
					buffer.writeByte('n');
				} else {
					buffer.writeByte(b);
				}

				count++;
				offset = i + 1;
			}
		}

		if (len > offset) {
			buffer.writeBytes(data, offset, len - offset);
		}

		return count;
	}

	public int writeTo(ChannelBuffer buffer) {
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

			count += writeRaw(buffer, data);
		}

		return count;
	}
}
