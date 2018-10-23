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

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.transaction.model.entity.*;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;
import com.dianping.cat.consumer.util.StringUtils;

import java.util.List;

public class TransactionReportMerger extends DefaultMerger {

	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);
	}

	private double[] mergeDoubleValue(String to, String from) {
		try {
			double[] result = new double[0];
			double[] source = new double[0];

			if (StringUtils.isNotEmpty(from)) {
				source = strToDoubleValue(StringUtils.split(from, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
			}

			if (StringUtils.isNotEmpty(to)) {
				result = strToDoubleValue(StringUtils.split(to, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
			} else if (source != null) {
				result = new double[source.length];
				for (int i = 0; i < source.length; i++) {
					result[i] = 0.0;
				}
			}

			for (int i = 0; i < result.length; i++) {
				result[i] += source[i];
			}

			return result;
		} catch (Exception e) {
			Cat.logError(e);
			return new double[0];
		}
	}

	@Override
	public void mergeDuration(Duration old, Duration duration) {
		old.setCount(old.getCount() + duration.getCount());
		old.setValue(duration.getValue());
	}

	@Override
	public void mergeGraph(Graph to, Graph from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		long[] count = mergeLongValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		String toSum = to.getSum();
		String fromSum = from.getSum();
		double[] sum = mergeDoubleValue(toSum, fromSum);
		to.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		String toFails = to.getFails();
		String fromFails = from.getFails();
		long[] fails = mergeLongValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		int length = count.length;
		double[] avg = new double[length];

		for (int i = 0; i < length; i++) {
			try {
				if (count[i] > 0) {
					avg[i] = sum[i] / count[i];
				} else {
					avg[i] = 0.0;
				}
			} catch (Exception e) {
				Cat.logError(e);
				avg[i] = 0.0;
			}
		}
		to.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
	}

	@Override
	public void mergeGraph2(Graph2 to, Graph2 from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		long[] count = mergeLongValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		String toSum = to.getSum();
		String fromSum = from.getSum();
		double[] sum = mergeDoubleValue(toSum, fromSum);
		to.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		String toFails = to.getFails();
		String fromFails = from.getFails();
		long[] fails = mergeLongValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		int length = count.length;
		double[] avg = new double[length];

		for (int i = 0; i < length; i++) {
			try {
				if (count[i] > 0) {
					avg[i] = sum[i] / count[i];
				} else {
					avg[i] = 0.0;
				}
			} catch (Exception e) {
				Cat.logError(e);
				avg[i] = 0.0;
			}
		}
		to.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
	}

	@Override
	public void mergeGraphTrend(GraphTrend to, GraphTrend from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		long[] count = mergeLongValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		String toSum = to.getSum();
		String fromSum = from.getSum();
		double[] sum = mergeDoubleValue(toSum, fromSum);
		to.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		String toFails = to.getFails();
		String fromFails = from.getFails();
		long[] fails = mergeLongValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_CHAR_SPLITTER));

		int length = count.length;
		double[] avg = new double[length];

		for (int i = 0; i < length; i++) {
			try {
				if (count[i] > 0) {
					avg[i] = sum[i] / count[i];
				} else {
					avg[i] = 0.0;
				}
			} catch (Exception e) {
				Cat.logError(e);
				avg[i] = 0.0;
			}
		}
		to.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
	}

	private long[] mergeLongValue(String to, String from) {
		try {
			long[] result = new long[0];
			long[] source = new long[0];

			if (StringUtils.isNotEmpty(from)) {
				source = strToLongValue(StringUtils.split(from, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
			}

			if (StringUtils.isNotEmpty(to)) {
				result = strToLongValue(StringUtils.split(to, GraphTrendUtil.GRAPH_CHAR_SPLITTER));
			} else if (source != null) {
				result = new long[source.length];
				for (int i = 0; i < source.length; i++) {
					result[i] = 0;
				}
			}

			for (int i = 0; i < source.length; i++) {
				result[i] += source[i];
			}

			return result;
		} catch (Exception e) {
			Cat.logError(e);
			return new long[0];
		}
	}

	@Override
	public void mergeMachine(Machine old, Machine machine) {
	}

	@Override
	public void mergeStatusCode(StatusCode old, StatusCode statusCode) {
		old.setCount(old.getCount() + statusCode.getCount());
	}

	@Override
	public void mergeName(TransactionName old, TransactionName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		if (totalCountSum > 0) {
			double line50Values = old.getLine50Value() * old.getTotalCount() + other.getLine50Value() * other.getTotalCount();
			double line90Values = old.getLine90Value() * old.getTotalCount() + other.getLine90Value() * other.getTotalCount();
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value() * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value() * other.getTotalCount();
			double line999Values = old.getLine999Value() * old.getTotalCount() + other.getLine999Value() * other.getTotalCount();
			double line9999Values =
									old.getLine9999Value() * old.getTotalCount() + other.getLine9999Value() * other.getTotalCount();

			old.setLine50Value(line50Values / totalCountSum);
			old.setLine90Value(line90Values / totalCountSum);
			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
			old.setLine999Value(line999Values / totalCountSum);
			old.setLine9999Value(line9999Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
			old.setLongestMessageUrl(other.getLongestMessageUrl());
		}

		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	public void mergeRange(Range old, Range range) {
		long totalCountSum = old.getCount() + range.getCount();

		if (totalCountSum > 0) {
			double line50Values = old.getLine50Value() * old.getCount() + range.getLine50Value() * range.getCount();
			double line90Values = old.getLine90Value() * old.getCount() + range.getLine90Value() * range.getCount();
			double line95Values = old.getLine95Value() * old.getCount() + range.getLine95Value() * range.getCount();
			double line99Values = old.getLine99Value() * old.getCount() + range.getLine99Value() * range.getCount();
			double line999Values = old.getLine999Value() * old.getCount() + range.getLine999Value() * range.getCount();
			double line9999Values = old.getLine9999Value() * old.getCount() + range.getLine9999Value() * range.getCount();

			old.setLine50Value(line50Values / totalCountSum);
			old.setLine90Value(line90Values / totalCountSum);
			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
			old.setLine999Value(line999Values / totalCountSum);
			old.setLine9999Value(line9999Values / totalCountSum);
		}

		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
		old.setSum(old.getSum() + range.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}

		if (range.getMin() < old.getMin()) {
			old.setMin(range.getMin());
		}

		if (range.getMax() > old.getMax()) {
			old.setMax(range.getMax());
		}
	}

	@Override
	public void mergeRange2(Range2 old, Range2 other) {
		long totalCountSum = old.getCount() + other.getCount();

		if (totalCountSum > 0) {
			double line50Values = old.getLine50Value() * old.getCount() + other.getLine50Value() * other.getCount();
			double line90Values = old.getLine90Value() * old.getCount() + other.getLine90Value() * other.getCount();
			double line95Values = old.getLine95Value() * old.getCount() + other.getLine95Value() * other.getCount();
			double line99Values = old.getLine99Value() * old.getCount() + other.getLine99Value() * other.getCount();
			double line999Values = old.getLine999Value() * old.getCount() + other.getLine999Value() * other.getCount();
			double line9999Values = old.getLine9999Value() * old.getCount() + other.getLine9999Value() * other.getCount();

			old.setLine50Value(line50Values / totalCountSum);
			old.setLine90Value(line90Values / totalCountSum);
			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
			old.setLine999Value(line999Values / totalCountSum);
			old.setLine9999Value(line9999Values / totalCountSum);
		}

		old.setCount(old.getCount() + other.getCount());
		old.setFails(old.getFails() + other.getFails());
		old.setSum(old.getSum() + other.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
		}
	}

	@Override
	public void mergeType(TransactionType old, TransactionType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		if (totalCountSum > 0) {
			double line50Values = old.getLine50Value() * old.getTotalCount() + other.getLine50Value() * other.getTotalCount();
			double line90Values = old.getLine90Value() * old.getTotalCount() + other.getLine90Value() * other.getTotalCount();
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value() * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value() * other.getTotalCount();
			double line999Values = old.getLine999Value() * old.getTotalCount() + other.getLine999Value() * other.getTotalCount();
			double line9999Values =
									old.getLine9999Value() * old.getTotalCount() + other.getLine9999Value() * other.getTotalCount();

			old.setLine50Value(line50Values / totalCountSum);
			old.setLine90Value(line90Values / totalCountSum);
			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
			old.setLine999Value(line999Values / totalCountSum);
			old.setLine9999Value(line9999Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
			old.setLongestMessageUrl(other.getLongestMessageUrl());
		}

		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
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

	private double[] strToDoubleValue(List<String> strs) {
		if (strs != null) {
			int size = strs.size();
			double[] result = new double[size];

			for (int i = 0; i < size; i++) {
				try {
					result[i] = Double.parseDouble(strs.get(i));
				} catch (Exception e) {
					result[i] = 0.0;
					Cat.logError(e);
				}
			}
			return result;
		}

		return null;
	}

	private long[] strToLongValue(List<String> strs) {
		if (strs != null) {
			int size = strs.size();
			long[] result = new long[size];

			for (int i = 0; i < size; i++) {
				try {
					result[i] = Long.parseLong(strs.get(i));
				} catch (Exception e) {
					result[i] = 0;
					Cat.logError(e);
				}
			}
			return result;
		}

		return null;
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		getTransactionReport().getIps().addAll(transactionReport.getMachines().keySet());
	}
}