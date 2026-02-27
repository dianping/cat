package com.dianping.cat.message.encoder;

import java.nio.charset.Charset;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.MetricBag;

import io.netty.buffer.ByteBuf;

public class NativeMetricBagEncoder implements MetricBagEncoder {
	public static final String ID = "NM1"; // native metric bag version 1

	@Override
	public void encode(MetricBag bag, ByteBuf buf) {
		Context ctx = new Context(buf);

		encodeHeader(ctx, bag);

		ctx.writeInt(bag.getMetrics().size());

		for (Metric metric : bag.getMetrics()) {
			encodeMetric(ctx, metric);
		}
	}

	private void encodeHeader(Context ctx, MetricBag bag) {
		ctx.writeVersion(ID);
		ctx.writeString(bag.getDomain());
		ctx.writeString(bag.getHostName());
		ctx.writeString(bag.getIpAddress());
	}

	private void encodeMetric(Context ctx, Metric metric) {
		ctx.writeLong(metric.getTimestamp());
		ctx.writeString(metric.getName());
		ctx.writeString(metric.getKind().name());
		ctx.writeInt(metric.getCount());
		ctx.writeLong(Double.doubleToLongBits(metric.getSum()));
		ctx.writeLong(metric.getDuration());
	}

	private static class Context {
		private static Charset UTF_8 = Charset.forName("UTF-8");

		private ByteBuf m_buf;

		public Context(ByteBuf buf) {
			m_buf = buf;
		}

		public void writeString(String str) {
			if (str == null) {
				m_buf.writeByte(-1);
			} else if (str.length() == 0) {
				writeVarint(0);
			} else {
				byte[] data = str.getBytes(UTF_8);

				writeVarint(data.length);
				m_buf.writeBytes(data);
			}
		}

		public void writeInt(int value) {
			writeVarint(value);
		}

		public void writeLong(long value) {
			writeVarint(value);
		}

		private void writeVarint(long value) {
			while (true) {
				if ((value & ~0x7FL) == 0) {
					m_buf.writeByte((byte) value);
					return;
				} else {
					m_buf.writeByte(((byte) value & 0x7F) | 0x80);
					value >>>= 7;
				}
			}
		}

		public void writeVersion(String version) {
			m_buf.writeBytes(version.getBytes());
		}
	}
}
