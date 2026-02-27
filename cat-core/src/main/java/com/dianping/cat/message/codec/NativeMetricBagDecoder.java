package com.dianping.cat.message.codec;

import java.nio.charset.Charset;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.MetricBag;
import com.dianping.cat.message.internal.DefaultMetricBag;

import io.netty.buffer.ByteBuf;

public class NativeMetricBagDecoder implements MetricBagDecoder {
	@Override
	public MetricBag decode(ByteBuf buf) {
		Context ctx = new Context(buf);
		DefaultMetricBag bag = new DefaultMetricBag();

		decodeHeader(ctx, bag);

		int size = (int) ctx.readInt();

		for (int i = 0; i < size; i++) {
			Metric metric = decodeMetric(ctx);

			bag.getMetrics().add(metric);
		}

		return bag;
	}

	private void decodeHeader(Context ctx, DefaultMetricBag bag) {
		String version = ctx.readVersion();

		if ("NM1".equals(version)) {
			String domain = ctx.readString();
			String hostName = ctx.readString();
			String ipAddress = ctx.readString();

			bag.setDomain(domain);
			bag.setHostName(hostName);
			bag.setIpAddress(ipAddress);
		} else {
			throw new RuntimeException(String.format("Unrecognized version(%s) for binary metric bag!", version));
		}
	}

	private Metric decodeMetric(Context ctx) {
		long timestamp = ctx.readLong();
		String name = ctx.readString();
		String kind = ctx.readString();
		int count = ctx.readInt();
		long sum = ctx.readLong();
		long duration = ctx.readLong();

		return new MyMetric(timestamp, name, kind, count, Double.longBitsToDouble(sum), duration);
	}

	private static class Context {
		private static Charset UTF_8 = Charset.forName("UTF-8");

		private ByteBuf m_buf;

		public Context(ByteBuf buf) {
			m_buf = buf;
		}

		public int readInt() {
			return (int) readVarint(32);
		}

		public long readLong() {
			return readVarint(64);
		}

		public String readString() {
			int readerIndex = m_buf.readerIndex();
			byte b = m_buf.getByte(readerIndex);

			if (b == -1) {
				return null;
			} else {
				m_buf.readerIndex(readerIndex);
			}

			int len = (int) readVarint(32);

			if (len == 0) {
				return "";
			}

			byte[] data = new byte[len];

			m_buf.readBytes(data, 0, len);
			return new String(data, 0, len, UTF_8);
		}

		private long readVarint(int length) {
			int shift = 0;
			long result = 0;

			while (shift < length) {
				final byte b = m_buf.readByte();
				result |= (long) (b & 0x7F) << shift;
				if ((b & 0x80) == 0) {
					return result;
				}
				shift += 7;
			}

			throw new RuntimeException("Malformed variable int " + length + "!");
		}

		public String readVersion() {
			byte[] bytes = new byte[3];

			m_buf.readBytes(bytes);

			return new String(bytes);
		}
	}

	private static class MyMetric implements Metric {
		private long m_timestamp;

		private String m_name;

		private Kind m_kind;

		private int m_count;

		private double m_sum;

		private long m_duration;

		public MyMetric(long timestamp, String name, String kind, int count, double sum, long duration) {
			m_timestamp = timestamp;
			m_name = name;
			m_kind = Kind.valueOf(kind);
			m_count = count;
			m_sum = sum;
			m_duration = duration;
		}

		@Override
		public void add(Metric metric) {
		}

		@Override
		public void count(int quantity) {
		}

		@Override
		public void duration(int count, long durationInMillis) {
		}

		@Override
		public int getCount() {
			return m_count;
		}

		@Override
		public long getDuration() {
			return m_duration;
		}

		@Override
		public Kind getKind() {
			return m_kind;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public double getSum() {
			return m_sum;
		}

		@Override
		public long getTimestamp() {
			return m_timestamp;
		}

		@Override
		public void sum(int count, double total) {
		}
	}
}
