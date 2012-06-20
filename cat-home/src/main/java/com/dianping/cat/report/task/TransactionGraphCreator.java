/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.hadoop.dal.Graph;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class TransactionGraphCreator implements GraphCreator<TransactionReport> {

	@Override
	public List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName, TransactionReport report) {
		Set<String> ips = report.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
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
				summaryBuilder.append(transactionType.getId());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getTotalCount());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getFailCount());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getMin());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getMax());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getSum());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getSum2());
				summaryBuilder.append('\n');

				String summaryKey = transactionType.getId();
				GraphLine summaryLine = allSummaryCache.get(summaryKey);
				if (summaryLine == null) {
					summaryLine = new GraphLine();
					allSummaryCache.put(summaryKey, summaryLine);
				}

				summaryLine.totalCount += transactionType.getTotalCount();
				summaryLine.failCount += transactionType.getFailCount();
				summaryLine.min += transactionType.getMin();
				summaryLine.max += transactionType.getMax();
				summaryLine.sum += transactionType.getSum();
				summaryLine.sum2 += transactionType.getSum2();
				Map<String, TransactionName> names = transactionType.getNames();
				for (Entry<String, TransactionName> nameEntry : names.entrySet()) {
					TransactionName transactionName = nameEntry.getValue();
					detailBuilder.append(transactionType.getId());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getId());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getTotalCount());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getFailCount());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getMin());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getMax());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getSum());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getSum2());
					detailBuilder.append('\n');

					String key = transactionType.getId() + "\t" + transactionName.getId();
					GraphLine detailLine = allDetailCache.get(key);
					if (detailLine == null) {
						detailLine = new GraphLine();
						allDetailCache.put(key, detailLine);
					}

					detailLine.totalCount += transactionName.getTotalCount();
					detailLine.failCount += transactionName.getFailCount();
					detailLine.min += transactionName.getMin();
					detailLine.max += transactionName.getMax();
					detailLine.sum += transactionName.getSum();
					detailLine.sum2 += transactionName.getSum2();
				}
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
			detailSb.append(value.totalCount);
			detailSb.append('\t');
			detailSb.append(value.failCount);
			detailSb.append('\t');
			detailSb.append(value.min);
			detailSb.append('\t');
			detailSb.append(value.max);
			detailSb.append('\t');
			detailSb.append(value.sum);
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
			summarySb.append(value.totalCount);
			summarySb.append('\t');
			summarySb.append(value.failCount);
			summarySb.append('\t');
			summarySb.append(value.min);
			summarySb.append('\t');
			summarySb.append(value.max);
			summarySb.append('\t');
			summarySb.append(value.sum);
			summarySb.append('\t');
			summarySb.append(value.sum2);
			summarySb.append('\n');
		}
		allGraph.setSummaryContent(summarySb.toString());

		graphs.add(allGraph);

		return graphs;

	}

}
