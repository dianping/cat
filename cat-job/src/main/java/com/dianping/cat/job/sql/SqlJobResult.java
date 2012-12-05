package com.dianping.cat.job.sql;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;

public class SqlJobResult implements Writable {

	private static final double LONG_TIME = 500;

	private static final char SPIT = '\t';

	private List<Double> m_durations = new ArrayList<Double>();

	private int m_failureCount;

	private int m_longTimeCount;

	private double m_max = Double.MIN_VALUE;

	private double m_min = Double.MAX_VALUE;

	private double m_sum;

	private double m_sum2;

	private List<String> m_urls = new ArrayList<String>();

	private Map<Integer, Integer> m_durationDistribution = new LinkedHashMap<Integer, Integer>();

	private Map<Integer, Integer> m_hitsOverTime = new LinkedHashMap<Integer, Integer>();

	private Map<Integer, Double> m_durationOverTime = new LinkedHashMap<Integer, Double>();

	private Map<Integer, Double> m_durationOverTimeSum = new LinkedHashMap<Integer, Double>();

	private Map<Integer, Integer> m_failureOverTime = new LinkedHashMap<Integer, Integer>();

	private DecimalFormat df = new DecimalFormat("#.##");

	public SqlJobResult() {
		for (int i = 0; i <= 60; i = i + 5) {
			m_hitsOverTime.put(i, 0);
			m_failureOverTime.put(i, 0);
			m_durationOverTime.put(i, 0.0);
			m_durationOverTimeSum.put(i, 0.0);
		}

		m_durationDistribution.put(0, 0);
		for (int i = 1; i <= 65536; i = i * 2) {
			m_durationDistribution.put(i, 0);
		}
	}

	public void add(double duration, int flag, String url, int minute) {
		m_sum += duration;
		m_sum2 += duration * duration;
		if (flag == 1) {
			m_failureCount++;
		}
		if (duration > LONG_TIME) {
			m_longTimeCount++;
		}
		if (duration < m_min) {
			m_min = duration;
		}
		if (duration > m_max) {
			m_max = duration;
		}
		m_durations.add(duration);

		int size = m_urls.size();
		if (size == 0) {
			m_urls.add(url);
		} else if (size == 1 && flag == 1) {
			m_urls.add(url);
		}
		int minuteKey = minute - minute % 5;
		m_hitsOverTime.put(minuteKey, m_hitsOverTime.get(minuteKey) + 1);

		m_durationOverTimeSum.put(minuteKey, m_durationOverTimeSum.get(minuteKey) + duration);

		if (flag == 1) {
			m_failureOverTime.put(minuteKey, m_failureOverTime.get(minuteKey) + 1);
		}
		int durationKey = getDuration(duration);
		m_durationDistribution.put(durationKey, m_durationDistribution.get(durationKey) + 1);
	}

	public int getDuration(double duration) {
		int min = -1;
		while (duration >= Math.pow(2, min + 1)) {
			min++;
		}
		return (int) Math.pow(2, min);
	}

	//get the 
	public double getPercent95Line() {
		Collections.sort(m_durations);
		int size = 95 * m_durations.size() / 100;
		return m_durations.get(size);
		/*
		double sum = 0;

		for (int i = 0; i < size; i++) {
			sum = sum + m_durations.get(i);
		}

		return sum / (double) size;
		 */	
		}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(m_durations.size()).append(SPIT);
		sb.append(m_failureCount).append(SPIT);
		sb.append(m_longTimeCount).append(SPIT);
		sb.append(df.format(m_min)).append(SPIT);
		sb.append(df.format(m_max)).append(SPIT);
		sb.append(df.format(m_sum)).append(SPIT);
		sb.append(df.format(m_sum2)).append(SPIT);
		sb.append(df.format(getPercent95Line())).append(SPIT);

		int size = m_urls.size();

		sb.append(m_urls.get(size - 1)).append(SPIT);
		sb.append(mapToString(m_durationDistribution)).append(SPIT);
		sb.append(mapToString(m_hitsOverTime)).append(SPIT);
		for (Entry<Integer, Double> entry : m_durationOverTimeSum.entrySet()) {
			Integer key = entry.getKey();
			double value = 0;
			int count = m_hitsOverTime.get(key);
			if (count > 0) {
				value = m_durationOverTimeSum.get(key) / count;
			}
			m_durationOverTime.put(key, value);
		}

		sb.append(map2String(m_durationOverTime)).append(SPIT);
		sb.append(mapToString(m_failureOverTime)).append(SPIT);
		return sb.toString();
	}

	private String map2String(Map<Integer, Double> map) {
		StringBuilder sb = new StringBuilder();
		if (map != null) {
			for (Entry<Integer, Double> entry : map.entrySet()) {
				sb.append(entry.getKey().toString()).append(":").append(entry.getValue().toString()).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	private String mapToString(Map<Integer, Integer> map) {
		StringBuilder sb = new StringBuilder();
		if (map != null) {
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				sb.append(entry.getKey().toString()).append(":").append(entry.getValue().toString()).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public List<Double> getDurations() {
		return m_durations;
	}

	public void setDurations(List<Double> durations) {
		m_durations = durations;
	}

	public int getFailureCount() {
		return m_failureCount;
	}

	public void setFailureCount(int failureCount) {
		m_failureCount = failureCount;
	}

	public int getLongTimeCount() {
		return m_longTimeCount;
	}

	public void setLongTimeCount(int longTimeCount) {
		m_longTimeCount = longTimeCount;
	}

	public double getMax() {
		return m_max;
	}

	public void setMax(double max) {
		m_max = max;
	}

	public double getMin() {
		return m_min;
	}

	public void setMin(double min) {
		m_min = min;
	}

	public double getSum() {
		return m_sum;
	}

	public void setSum(double sum) {
		m_sum = sum;
	}

	public double getSum2() {
		return m_sum2;
	}

	public void setSum2(double sum2) {
		m_sum2 = sum2;
	}

	public Map<Integer, Integer> getDurationDistribution() {
		return m_durationDistribution;
	}

	public void setDurationDistribution(Map<Integer, Integer> durationDistribution) {
		m_durationDistribution = durationDistribution;
	}

	public Map<Integer, Integer> getHitsOverTime() {
		return m_hitsOverTime;
	}

	public void setHitsOverTime(Map<Integer, Integer> hitsOverTime) {
		m_hitsOverTime = hitsOverTime;
	}

	public Map<Integer, Double> getDurationOverTime() {
		return m_durationOverTime;
	}

	public void setDurationOverTime(Map<Integer, Double> durationOverTime) {
		m_durationOverTime = durationOverTime;
	}

	public Map<Integer, Integer> getFailureOverTime() {
		return m_failureOverTime;
	}

	public void setFailureOverTime(Map<Integer, Integer> failureOverTime) {
		m_failureOverTime = failureOverTime;
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		throw new UnsupportedOperationException(
		      "This method should never be called, please check with the author if any problem.");
	}
}