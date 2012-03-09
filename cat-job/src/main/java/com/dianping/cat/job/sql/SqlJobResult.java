package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.Writable;

public class SqlJobResult implements Writable {

	private static final double LONG_TIME = 50;

	private static final char SPIT = '\t';

	private List<Double> m_durations = new ArrayList<Double>();

	private int m_failureCount;

	private int m_longTimeCount;

	private double m_max = Double.MIN_VALUE;

	private double m_min = Double.MAX_VALUE;

	private double m_sum;

	private double m_sum2;

	private List<String> m_urls = new ArrayList<String>();

	private DecimalFormat df = new DecimalFormat("#.##");

	public void add(double value, int flag, String url ) {
		m_sum += value;
		m_sum2 += value * value;
		if (flag == 1) {
			m_failureCount++;
		}
		if (value > LONG_TIME) {
			m_longTimeCount++;
		}
		if (value < m_min) {
			m_min = value;
		}
		if (value > m_max) {
			m_max = value;
		}
		m_durations.add(value);

		int size = m_urls.size();
		if (size == 0) {
			m_urls.add(url);
		} else if (size == 1 && flag == 1) {
			m_urls.add(url);
		}
	}

	public double getAvg() {
		Collections.sort(m_durations);
		int size = 95 * m_durations.size() / 100;
		double sum = 0;

		for (int i = 0; i < size; i++) {
			sum = sum + m_durations.get(i);
		}

		return sum / (double) size;
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(m_durations.size()).append(SPIT).append(m_failureCount).append(SPIT).append(m_longTimeCount)
		      .append(SPIT);
		sb.append(df.format(m_min)).append(SPIT).append(df.format(m_max)).append(SPIT).append(df.format(m_sum))
		      .append(SPIT).append(df.format(m_sum2)).append(SPIT).append(df.format(getAvg())).append(SPIT);
		
		int size = m_urls.size();
		
		sb.append(m_urls.get(size-1));
		return sb.toString();
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}
}