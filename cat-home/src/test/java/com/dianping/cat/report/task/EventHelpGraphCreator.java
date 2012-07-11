/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.hadoop.dal.Graph;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class EventHelpGraphCreator implements GraphCreator<EventReport> {

	@Override
	public List<Graph> splitReportToGraphs(Date reportPeriod, String domainName, String reportName, EventReport eventReport) {
			Set<String> ips = eventReport.getIps();
			List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
			Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
			Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();
			Date creationDate = new Date();
			for (String ip : ips) {
				Graph graph = new Graph();
				graph.setIp(ip);
				graph.setDomain(domainName);
				graph.setName(reportName);
				graph.setPeriod(reportPeriod);
				graph.setType(3);
				graph.setCreationDate(creationDate);
				com.dianping.cat.consumer.event.model.entity.Machine machine = eventReport.getMachines().get(ip);
				Map<String, EventType> types = machine.getTypes();
				StringBuilder detailBuilder = new StringBuilder();
				StringBuilder summaryBuilder = new StringBuilder();
				for (Entry<String, EventType> eventEntry : types.entrySet()) {
					EventType eventType = eventEntry.getValue();
					summaryBuilder.append(eventType.getId());
					summaryBuilder.append('\t');
					summaryBuilder.append(eventType.getTotalCount());
					summaryBuilder.append('\t');
					summaryBuilder.append(eventType.getFailCount());
					summaryBuilder.append('\n');

					String summaryKey = eventType.getId();
					GraphLine summaryLine = allSummaryCache.get(summaryKey);
					if (summaryLine == null) {
						summaryLine = new GraphLine();
						allSummaryCache.put(summaryKey, summaryLine);
					}

					summaryLine.totalCount += eventType.getTotalCount();
					summaryLine.failCount += eventType.getFailCount();
					Map<String, EventName> names = eventType.getNames();
					for (Entry<String, EventName> nameEntry : names.entrySet()) {
						EventName eventName = nameEntry.getValue();
						detailBuilder.append(eventType.getId());
						detailBuilder.append('\t');
						detailBuilder.append(eventName.getId());
						detailBuilder.append('\t');
						detailBuilder.append(eventName.getTotalCount());
						detailBuilder.append('\t');
						detailBuilder.append(eventName.getFailCount());
						detailBuilder.append('\n');

						String key = eventType.getId() + "\t" + eventName.getId();
						GraphLine detailLine = allDetailCache.get(key);
						if (detailLine == null) {
							detailLine = new GraphLine();
							allDetailCache.put(key, detailLine);
						}

						detailLine.totalCount += eventName.getTotalCount();
						detailLine.failCount += eventName.getFailCount();
					}
				}
				graph.setDetailContent(detailBuilder.toString());
				graph.setSummaryContent(summaryBuilder.toString());
				graphs.add(graph);
			}

			Graph allGraph = new Graph();
			allGraph.setIp("all");
			allGraph.setDomain(domainName);
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
				summarySb.append('\n');
			}
			allGraph.setSummaryContent(summarySb.toString());

			graphs.add(allGraph);

			return graphs;

	}

}
