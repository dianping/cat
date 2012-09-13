/**
 * 
 */
package com.dianping.cat.report.task.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.report.task.spi.GraphCreator;
import com.dianping.cat.report.task.spi.GraphLine;

public class TransactionGraphCreator implements GraphCreator<TransactionReport> {

	private double[] arrayAdd(double[] src, double added[]) {
		int size = added.length;
		if (src == null) {
			src = new double[size];
		}
		for (int i = 0; i < size; i++) {
			src[i] = src[i] + added[i];
		}
		return src;
	}

	private long[] arrayAdd(long[] src, long added[]) {
		int size = added.length;
		if (src == null) {
			src = new long[size];
		}
		for (int i = 0; i < size; i++) {
			src[i] = src[i] + added[i];
		}
		return src;
	}

	private String arrayToString(double[] array) {
		StringBuilder sb = new StringBuilder();
		int size = 12;
		for (int i = 0; i < size; i++) {
			sb.append(array[i]);
			if (i < 12) {
				sb.append(',');
			}
		}
		return sb.toString();
	}

	private String arrayToString(long[] array) {
		StringBuilder sb = new StringBuilder();
		int size = 12;
		for (int i = 0; i < size; i++) {
			sb.append(array[i]);
			if (i < 11) {
				sb.append(',');
			}
		}
		return sb.toString();
	}

	private long[] getFailsCount(List<Range> ranges) {
		long[] value = new long[12];
		for (Range range : ranges) {
			int minute = range.getValue();
			value[minute / 5] = range.getFails();
		}
		return value;
	}

	private double[] getSumCount(List<Range> ranges) {
		double[] value = new double[12];
		for (Range range : ranges) {
			int minute = range.getValue();
			value[minute / 5] = range.getSum();
		}
		return value;
	}

	private long[] getTotalCount(List<Range> ranges) {
		long[] value = new long[12];
		for (Range range : ranges) {
			int minute = range.getValue();
			value[minute / 5] = range.getCount();
		}
		return value;
	}

	@Override
	public List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName,
	      TransactionReport report) {
		Set<String> ips = report.getIps();
		// all and every minute
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1);
		Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
		Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();

		Date creationDate = new Date();
		for (String ip : ips) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(reportDomain);
			graph.setName(reportName);
			graph.setPeriod(reportPeriod);
			graph.setType(3);
			graph.setCreationDate(creationDate);
			Machine machine = report.getMachines().get(ip);
			Map<String, TransactionType> types = machine.getTypes();
			StringBuilder detailBuilder = new StringBuilder();
			StringBuilder summaryBuilder = new StringBuilder();
			for (Entry<String, TransactionType> transactionEntry : types.entrySet()) {
				TransactionType transactionType = transactionEntry.getValue();
				long[] typeCounts = new long[12];
				long[] typeFails = new long[12];
				double[] typeSums = new double[12];

				Map<String, TransactionName> names = transactionType.getNames();
				for (Entry<String, TransactionName> nameEntry : names.entrySet()) {
					TransactionName transactionName = nameEntry.getValue();
					List<Range> ranges = transactionName.getRanges();

					detailBuilder.append(transactionType.getId());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getId());
					detailBuilder.append('\t');
					long[] totalCount = getTotalCount(ranges);
					detailBuilder.append(arrayToString(totalCount));
					detailBuilder.append('\t');
					long[] failsCount = getFailsCount(ranges);
					detailBuilder.append(arrayToString(failsCount));
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getMin());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getMax());
					detailBuilder.append('\t');
					double[] sumCount = getSumCount(ranges);
					detailBuilder.append(arrayToString(sumCount));
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getSum2());
					detailBuilder.append('\n');

					String key = transactionType.getId() + "\t" + transactionName.getId();
					GraphLine detailLine = allDetailCache.get(key);
					if (detailLine == null) {
						detailLine = new GraphLine();
						allDetailCache.put(key, detailLine);
					}

					// detailLine.totalCount += transactionName.getTotalCount();
					detailLine.totalCounts = arrayAdd(detailLine.totalCounts, totalCount);
					// detailLine.failCount += transactionName.getFailCount();
					detailLine.failCounts = arrayAdd(detailLine.failCounts, failsCount);
					detailLine.min += transactionName.getMin();
					detailLine.max += transactionName.getMax();
					// detailLine.sum += transactionName.getSum();
					detailLine.sums = arrayAdd(detailLine.sums, sumCount);
					detailLine.sum2 += transactionName.getSum2();

					typeCounts = arrayAdd(typeCounts, totalCount);
					typeFails = arrayAdd(typeFails, failsCount);
					typeSums = arrayAdd(typeSums, sumCount);
				}
				summaryBuilder.append(transactionType.getId());
				summaryBuilder.append('\t');
				// summaryBuilder.append(transactionType.getTotalCount());
				summaryBuilder.append(arrayToString(typeCounts));
				summaryBuilder.append('\t');
				// summaryBuilder.append(transactionType.getFailCount());
				summaryBuilder.append(arrayToString(typeFails));
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getMin());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getMax());
				summaryBuilder.append('\t');
				// summaryBuilder.append(transactionType.getSum());
				summaryBuilder.append(arrayToString(typeSums));
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getSum2());
				summaryBuilder.append('\n');

				String summaryKey = transactionType.getId();
				GraphLine summaryLine = allSummaryCache.get(summaryKey);
				if (summaryLine == null) {
					summaryLine = new GraphLine();
					allSummaryCache.put(summaryKey, summaryLine);
				}

				summaryLine.totalCounts = arrayAdd(summaryLine.totalCounts, typeCounts);
				summaryLine.failCounts = arrayAdd(summaryLine.failCounts, typeFails);
				summaryLine.min += transactionType.getMin();
				summaryLine.max += transactionType.getMax();
				// summaryLine.sum += transactionType.getSum();
				summaryLine.sums = arrayAdd(summaryLine.sums, typeSums);
				summaryLine.sum2 += transactionType.getSum2();
			}
			graph.setDetailContent(detailBuilder.toString());
			graph.setSummaryContent(summaryBuilder.toString());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp("all");
		allGraph.setDomain(reportDomain);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeriod);
		allGraph.setType(3);
		allGraph.setCreationDate(creationDate);

		StringBuilder detailSb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allDetailCache.entrySet()) {
			detailSb.append(entry.getKey());
			detailSb.append('\t');
			GraphLine value = entry.getValue();
			detailSb.append(arrayToString(value.totalCounts));
			detailSb.append('\t');
			detailSb.append(arrayToString(value.failCounts));
			detailSb.append('\t');
			detailSb.append(value.min);
			detailSb.append('\t');
			detailSb.append(value.max);
			detailSb.append('\t');
			detailSb.append(arrayToString(value.sums));
			detailSb.append('\t');
			detailSb.append(value.sum2);
			detailSb.append('\n');
		}
		allGraph.setDetailContent(detailSb.toString());

		StringBuilder summarySb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allSummaryCache.entrySet()) {
			summarySb.append(entry.getKey());
			summarySb.append('\t');
			GraphLine value = entry.getValue();
			summarySb.append(arrayToString(value.totalCounts));
			summarySb.append('\t');
			summarySb.append(arrayToString(value.failCounts));
			summarySb.append('\t');
			summarySb.append(value.min);
			summarySb.append('\t');
			summarySb.append(value.max);
			summarySb.append('\t');
			summarySb.append(arrayToString(value.sums));
			summarySb.append('\t');
			summarySb.append(value.sum2);
			summarySb.append('\n');
		}
		allGraph.setSummaryContent(summarySb.toString());

		graphs.add(allGraph);

		return graphs;

	}
}
