/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.consumer.transaction;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TransactionStatisticsComputer extends BaseVisitor {

	public final static double PERCENT_50 = 50.0;

	public final static double PERCENT_90 = 90.0;

	public final static double PERCENT_95 = 95.0;

	public final static double PERCENT_99 = 99.0;

	public final static double PERCENT_999 = 99.9;

	public final static double PERCENT_9999 = 99.99;

	private double m_duration = 3600;

	private boolean m_clearAll = false;

	private int m_maxDurationMinute = 1;

	public TransactionStatisticsComputer() {
	}

	public TransactionStatisticsComputer(boolean clearAll) {
		m_clearAll = clearAll;
	}

	public Map<Double, Integer> computeLineValue(Map<Integer, AllDuration> sorted, double[] percents) {
		int totalCount = 0;

		for (AllDuration duration : sorted.values()) {
			totalCount += duration.getCount();
		}

		Map<Double, Integer> lineValue = new LinkedHashMap<Double, Integer>();
		Map<Double, Integer> remainings = new LinkedHashMap<>();

		for (double percent : percents) {
			int remaining = (int) (totalCount * (100 - percent) / 100);

			remainings.put(percent, remaining);
			lineValue.put(percent, 0);
		}

		for (Entry<Integer, AllDuration> entry : sorted.entrySet()) {
			int count = entry.getValue().getCount();
			int result = 1;

			for (double key : percents) {
				if (lineValue.get(key) == 0) {
					int remaining = remainings.get(key);
					remaining -= count;

					if (remaining <= 0) {
						lineValue.put(key, entry.getKey());
					}
					remainings.put(key, remaining);
				}
				result &= lineValue.get(key);
			}

			if (result > 0) {
				break;
			}
		}

		return lineValue;
	}

	public int getMaxDurationMinute() {
		return m_maxDurationMinute;
	}

	public void setMaxDurationMinute(int maxDurationMinute) {
		m_maxDurationMinute = maxDurationMinute;
	}

	public TransactionStatisticsComputer setDuration(double duration) {
		m_duration = duration;
		return this;
	}

	public Map<Integer, AllDuration> sortMap(Map<Integer, AllDuration> durations) {
		Map<Integer, AllDuration> sorted = new TreeMap<Integer, AllDuration>(TransactionComparator.DESC);

		sorted.putAll(durations);

		return sorted;
	}

	double std(long count, double avg, double sum2, double max) {
		double value = sum2 / count - avg * avg;

		if (value <= 0 || count <= 1) {
			return 0;
		} else if (count == 2) {
			return max - avg;
		} else {
			return Math.sqrt(value);
		}
	}

	@Override
	public void visitName(TransactionName name) {
		super.visitName(name);

		long count = name.getTotalCount();

		if (count > 0) {
			long failCount = name.getFailCount();
			double avg = name.getSum() / count;
			double std = std(count, avg, name.getSum2(), name.getMax());
			double failPercent = 100.0 * failCount / count;

			name.setFailPercent(failPercent);
			name.setAvg(avg);
			name.setStd(std);

			final Map<Integer, AllDuration> allDurations = name.getAllDurations();

			if (!allDurations.isEmpty()) {
				final Map<Integer, AllDuration> sourtMap = sortMap(allDurations);
				Map<Double, Integer> lineValues = computeLineValue(sourtMap,
										new double[] { PERCENT_50, PERCENT_90, PERCENT_95, PERCENT_99, PERCENT_999, PERCENT_9999 });

				name.setLine50Value(lineValues.get(PERCENT_50));
				name.setLine90Value(lineValues.get(PERCENT_90));
				name.setLine95Value(lineValues.get(PERCENT_95));
				name.setLine99Value(lineValues.get(PERCENT_99));
				name.setLine999Value(lineValues.get(PERCENT_999));
				name.setLine9999Value(lineValues.get(PERCENT_9999));
			}
		}
		if (m_duration > 0) {
			name.setTps(name.getTotalCount() * 1.0 / m_duration);
		}

		if (m_clearAll) {
			name.getAllDurations().clear();
		}
	}

	@Override
	public void visitRange(Range range) {
		if (range.getCount() > 0) {
			range.setAvg(range.getSum() / range.getCount());
		}

		final Map<Integer, AllDuration> allDurations = range.getAllDurations();

		if (!allDurations.isEmpty() && !range.getClearDuration()) {
			final Map<Integer, AllDuration> sourtMap = sortMap(allDurations);
			Map<Double, Integer> lineValues = computeLineValue(sourtMap,
									new double[] { PERCENT_50, PERCENT_90, PERCENT_95, PERCENT_99, PERCENT_999, PERCENT_9999 });

			range.setLine50Value(lineValues.get(PERCENT_50));
			range.setLine90Value(lineValues.get(PERCENT_90));
			range.setLine95Value(lineValues.get(PERCENT_95));
			range.setLine99Value(lineValues.get(PERCENT_99));
			range.setLine999Value(lineValues.get(PERCENT_999));
			range.setLine9999Value(lineValues.get(PERCENT_9999));
		}

		// clear duration in type all duration
		long current = System.currentTimeMillis() / 1000 / 60;
		int min = (int) (current % (60));

		if (!allDurations.isEmpty() && range.getValue() + m_maxDurationMinute < min) {
			range.getAllDurations().clear();
			range.setClearDuration(true);
		}

		if (m_clearAll) {
			range.getAllDurations().clear();
		}
	}

	@Override
	public void visitRange2(Range2 range2) {
		if (range2.getCount() > 0) {
			range2.setAvg(range2.getSum() / range2.getCount());
		}

		final Map<Integer, AllDuration> allDurations = range2.getAllDurations();

		if (!allDurations.isEmpty() && !range2.getClearDuration()) {
			final Map<Integer, AllDuration> sourtMap = sortMap(allDurations);
			Map<Double, Integer> lineValues = computeLineValue(sourtMap,
									new double[] { PERCENT_50, PERCENT_90, PERCENT_95, PERCENT_99, PERCENT_999, PERCENT_9999 });

			range2.setLine50Value(lineValues.get(PERCENT_50));
			range2.setLine90Value(lineValues.get(PERCENT_90));
			range2.setLine95Value(lineValues.get(PERCENT_95));
			range2.setLine99Value(lineValues.get(PERCENT_99));
			range2.setLine999Value(lineValues.get(PERCENT_999));
			range2.setLine9999Value(lineValues.get(PERCENT_9999));
		}

		// clear duration in name all duration
		long current = System.currentTimeMillis() / 1000 / 60;
		int min = (int) (current % (60));

		if (!allDurations.isEmpty() && range2.getValue() + m_maxDurationMinute < min) {
			range2.getAllDurations().clear();
			range2.setClearDuration(true);
		}

		if (m_clearAll) {
			range2.getAllDurations().clear();
		}
	}

	@Override
	public void visitType(TransactionType type) {
		super.visitType(type);

		long count = type.getTotalCount();

		if (count > 0) {
			long failCount = type.getFailCount();
			double avg = type.getSum() / count;
			double std = std(count, avg, type.getSum2(), type.getMax());
			double failPercent = 100.0 * failCount / count;

			type.setFailPercent(failPercent);
			type.setAvg(avg);
			type.setStd(std);

			final Map<Integer, AllDuration> allDurations = type.getAllDurations();

			if (!allDurations.isEmpty()) {
				final Map<Integer, AllDuration> sourtMap = sortMap(allDurations);
				Map<Double, Integer> lineValues = computeLineValue(sourtMap,
										new double[] { PERCENT_50, PERCENT_90, PERCENT_95, PERCENT_99, PERCENT_999, PERCENT_9999 });

				type.setLine50Value(lineValues.get(PERCENT_50));
				type.setLine90Value(lineValues.get(PERCENT_90));
				type.setLine95Value(lineValues.get(PERCENT_95));
				type.setLine99Value(lineValues.get(PERCENT_99));
				type.setLine999Value(lineValues.get(PERCENT_999));
				type.setLine9999Value(lineValues.get(PERCENT_9999));
			}

			if (m_duration > 0) {
				type.setTps(type.getTotalCount() * 1.0 / m_duration);
			}
		}
		if (m_clearAll) {
			type.getAllDurations().clear();
		}
	}

	private enum TransactionComparator implements Comparator<Integer> {
		DESC {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		}
	}
}