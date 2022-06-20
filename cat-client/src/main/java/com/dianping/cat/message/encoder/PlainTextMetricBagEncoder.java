package com.dianping.cat.message.encoder;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.internal.MetricBag;

import io.netty.buffer.ByteBuf;

// Component
public class PlainTextMetricBagEncoder implements MetricBagEncoder {
	public static final String ID = "PB1"; // plain text metric bag version 1

	private static final byte TAB = '\t'; // tab character

	private static final byte LF = '\n'; // line feed character

	private BufferHelper m_bufferHelper = new BufferHelper();

	private DateHelper m_dateHelper = new DateHelper();

	@Override
	public void encode(MetricBag bag, ByteBuf buf) {
		encodeHeader(buf, bag);

		for (Metric metric : bag.getMetrics()) {
			encodeMetric(buf, metric);
		}
	}

	private void encodeHeader(ByteBuf buf, MetricBag bag) {
		BufferHelper helper = m_bufferHelper;

		helper.write(buf, ID);
		helper.write(buf, TAB);
		helper.write(buf, bag.getDomain());
		helper.write(buf, TAB);
		helper.write(buf, bag.getHostName());
		helper.write(buf, TAB);
		helper.write(buf, bag.getIpAddress());
		helper.write(buf, LF);
	}

	private void encodeMetric(ByteBuf buf, Metric metric) {
		BufferHelper helper = m_bufferHelper;

		helper.write(buf, m_dateHelper.format(metric.getTimestamp()));
		helper.write(buf, TAB);
		helper.writeRaw(buf, metric.getName());
		helper.write(buf, TAB);
		helper.writeRaw(buf, metric.getKind().name());
		helper.write(buf, TAB);
		helper.writeRaw(buf, Integer.toString(metric.getCount()));
		helper.write(buf, TAB);
		helper.writeRaw(buf, Double.toString(metric.getSum()));
		helper.write(buf, TAB);
		helper.writeRaw(buf, Long.toString(metric.getDuration()));
		helper.write(buf, LF);
	}

	private static class BufferHelper {
		private void escape(ByteBuf buf, byte[] data) {
			int len = data.length;
			int offset = 0;

			for (int i = 0; i < len; i++) {
				byte b = data[i];

				if (b == '\t' || b == '\r' || b == '\n' || b == '\\') {
					buf.writeBytes(data, offset, i - offset);
					buf.writeByte('\\');

					if (b == '\t') {
						buf.writeByte('t');
					} else if (b == '\r') {
						buf.writeByte('r');
					} else if (b == '\n') {
						buf.writeByte('n');
					} else {
						buf.writeByte(b);
					}

					offset = i + 1;
				}
			}

			if (len > offset) {
				buf.writeBytes(data, offset, len - offset);
			}
		}

		public void write(ByteBuf buf, byte b) {
			buf.writeByte(b);
		}

		public void write(ByteBuf buf, String str) {
			if (str == null) {
				str = "null";
			}

			byte[] data = str.getBytes();

			buf.writeBytes(data);
		}

		public void writeRaw(ByteBuf buf, String str) {
			if (str == null) {
				str = "null";
			}

			byte[] data;

			try {
				data = str.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				data = str.getBytes();
			}

			escape(buf, data);
		}
	}

	/**
	 * Thread safe date helper class. DateFormat is NOT thread safe.
	 */
	private static class DateHelper {
		private BlockingQueue<SimpleDateFormat> m_formats = new ArrayBlockingQueue<SimpleDateFormat>(20);

		public String format(long timestamp) {
			SimpleDateFormat format = m_formats.poll();

			if (format == null) {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			}

			try {
				return format.format(new Date(timestamp));
			} finally {
				if (m_formats.remainingCapacity() > 0) {
					m_formats.offer(format);
				}
			}
		}
	}
}