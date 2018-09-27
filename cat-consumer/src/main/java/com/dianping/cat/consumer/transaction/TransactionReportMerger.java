package com.dianping.cat.consumer.transaction;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;

public class TransactionReportMerger extends DefaultMerger {

	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);
	}

	@Override
	public void mergeDuration(Duration old, Duration duration) {
		old.setCount(old.getCount() + duration.getCount());
		old.setValue(duration.getValue());
	}

	@Override
	public void mergeMachine(Machine old, Machine machine) {
	}

	@Override
	public void mergeName(TransactionName old, TransactionName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();
		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
			      * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
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
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
		old.setSum(old.getSum() + range.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	@Override
	public void mergeType(TransactionType old, TransactionType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();
		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
			      * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
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
	public void mergeGraph(Graph to, Graph from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		Integer[] count = mergeIntegerValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_SPLITTER));

		String toSum = to.getSum();
		String fromSum = from.getSum();
		Double[] sum = mergeDoubleValue(toSum, fromSum);
		to.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_SPLITTER));

		String toFails = to.getFails();
		String fromFails = from.getFails();
		Integer[] fails = mergeIntegerValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));

		int length = count.length;
		Double[] avg = new Double[length];

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
		to.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_SPLITTER));
	}

	@Override
	public void mergeGraph2(Graph2 to, Graph2 from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		Integer[] count = mergeIntegerValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_SPLITTER));

		String toSum = to.getSum();
		String fromSum = from.getSum();
		Double[] sum = mergeDoubleValue(toSum, fromSum);
		to.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_SPLITTER));

		String toFails = to.getFails();
		String fromFails = from.getFails();
		Integer[] fails = mergeIntegerValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));

		int length = count.length;
		Double[] avg = new Double[length];

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
		to.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_SPLITTER));
	}
	
	@Override
	public void mergeGraphTrend(GraphTrend to, GraphTrend from) {
		String toCount = to.getCount();
		String fromCount = from.getCount();
		Integer[] count = mergeIntegerValue(toCount, fromCount);
		to.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_SPLITTER));

		String toSum = to.getSum();
		String fromSum = from.getSum();
		Double[] sum = mergeDoubleValue(toSum, fromSum);
		to.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_SPLITTER));

		String toFails = to.getFails();
		String fromFails = from.getFails();
		Integer[] fails = mergeIntegerValue(toFails, fromFails);
		to.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));

		int length = count.length;
		Double[] avg = new Double[length];

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
		to.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_SPLITTER));
	}

	private Double[] mergeDoubleValue(String to, String from) {
		Double[] result = null;
		Double[] source = null;

		if (StringUtils.isNotBlank(from)) {
			source = strToDoubleValue(from.split(GraphTrendUtil.GRAPH_SPLITTER));
		}

		if (StringUtils.isNotBlank(to)) {
			result = strToDoubleValue(to.split(GraphTrendUtil.GRAPH_SPLITTER));
		} else if (source != null) {
			result = new Double[source.length];
			for (int i = 0; i < source.length; i++) {
				result[i] = 0.0;
			}
		}

		for (int i = 0; i < result.length; i++) {
			result[i] += source[i];
		}

		return result;
	}

	private Double[] strToDoubleValue(String[] strs) {
		if (strs != null) {
			int size = strs.length;
			Double[] result = new Double[size];

			for (int i = 0; i < size; i++) {
				try {
					result[i] = Double.parseDouble(strs[i]);
				} catch (Exception e) {
					result[i] = 0.0;
					Cat.logError(e);
				}
			}
			return result;
		}

		return null;
	}

	private Integer[] mergeIntegerValue(String to, String from) {
		Integer[] result = null;
		Integer[] source = null;

		if (StringUtils.isNotBlank(from)) {
			source = strToIntegerValue(from.split(GraphTrendUtil.GRAPH_SPLITTER));
		}

		if (StringUtils.isNotBlank(to)) {
			result = strToIntegerValue(to.split(GraphTrendUtil.GRAPH_SPLITTER));
		} else if (source != null) {
			result = new Integer[source.length];
			for (int i = 0; i < source.length; i++) {
				result[i] = 0;
			}
		}

		for (int i = 0; i < source.length; i++) {
			result[i] += source[i];
		}

		return result;
	}

	private Integer[] strToIntegerValue(String[] strs) {
		if (strs != null) {
			int size = strs.length;
			Integer[] result = new Integer[size];

			for (int i = 0; i < size; i++) {
				try {
					result[i] = Integer.parseInt(strs[i]);
				} catch (Exception e) {
					result[i] = 0;
					Cat.logError(e);
				}
			}
			return result;
		}

		return null;
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
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		getTransactionReport().getIps().addAll(transactionReport.getIps());
	}
}
