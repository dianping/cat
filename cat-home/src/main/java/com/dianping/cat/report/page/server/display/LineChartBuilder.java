package com.dianping.cat.report.page.server.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.server.service.MetricGraphBuilder;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.server.MetricType;
import com.dianping.cat.server.QueryParameter;

@Named
public class LineChartBuilder {

	@Inject
	private MetricService m_metricService;

	@Inject
	private MetricGraphBuilder m_graphBuilder;

	private static ExecutorService s_threadPool = Threads.forPool().getFixedThreadPool("Cat-metric", 100);

	public List<LineChart> buildLineCharts(Date start, Date end, String interval, String view, Graph graph) {
		List<LinechartParameter> parameters = new LinkedList<LinechartParameter>();

		for (Entry<String, Item> entry : graph.getItems().entrySet()) {
			Item item = entry.getValue();

			if (StringUtils.isEmpty(view) || view.equals(item.getView())) {
				LineChart linechart = new LineChart();

				linechart.setId(entry.getKey());
				linechart.setHtmlTitle(entry.getKey());

				for (Entry<String, Segment> e : item.getSegments().entrySet()) {
					try {
						Segment segment = e.getValue();
						MetricType type = MetricType.getByName(segment.getType(), MetricType.AVG);
						QueryParameter parameter = new QueryParameter();

						parameter.setCategory(segment.getCategory()).setStart(start).setEnd(end)
						      .setMeasurement(segment.getMeasure()).setType(type).setTags(segment.getTags())
						      .setInterval(interval).setFillValue("0");
						parameters.add(new LinechartParameter(entry.getKey(), segment.getId(), parameter));
					} catch (Exception ex) {
						Cat.logError(ex);
					}
				}
			}
		}
		return parallelFetchData(parameters);
	}

	public List<LineChart> buildLineCharts(Date start, Date end, String interval, String endPoint,
	      List<com.dianping.cat.home.server.entity.Item> items) {
		List<LinechartParameter> parameters = new LinkedList<LinechartParameter>();

		for (com.dianping.cat.home.server.entity.Item item : items) {
			for (com.dianping.cat.home.server.entity.Segment segment : item.getSegments().values()) {
				String measurement = segment.getId();
				List<String> measures = m_metricService.queryMeasurements(segment.getCategory(), measurement,
				      Arrays.asList(endPoint));
				List<String> results = parseSeries(measures);
				Collections.sort(results);
				// TODO

				if (!results.isEmpty()) {
					for (String tag : results) {
						MetricType type = MetricType.getByName(segment.getType(), MetricType.AVG);
						QueryParameter parameter = new QueryParameter();

						parameter.setCategory(segment.getCategory()).setStart(start).setEnd(end)
						      .setMeasurement(segment.getId()).setType(type).setTags(m_graphBuilder.buildTag(tag, endPoint))
						      .setInterval(interval).setFillValue("0");
						parameters.add(new LinechartParameter(measurement, tag, parameter));
					}
				} else {
					MetricType type = MetricType.getByName(segment.getType(), MetricType.AVG);
					QueryParameter parameter = new QueryParameter();

					parameter.setCategory(segment.getCategory()).setStart(start).setEnd(end).setMeasurement(segment.getId())
					      .setType(type).setTags(m_graphBuilder.buildTag("", endPoint)).setInterval(interval)
					      .setFillValue("0");
					parameters.add(new LinechartParameter(measurement, measurement, parameter));
				}
			}
		}
		return parallelFetchData(parameters);
	}

	private List<LineChart> parallelFetchData(List<LinechartParameter> queries) {
		final Map<String, Map<String, Map<Long, Double>>> linecharts = new ConcurrentHashMap<String, Map<String, Map<Long, Double>>>();
		final Semaphore semaphore = new Semaphore(0);
		final Transaction t = Cat.newTransaction("MetricService", "server");

		t.setStatus(Message.SUCCESS);

		for (final LinechartParameter query : queries) {
			s_threadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						String linechartId = query.getId();
						Map<Long, Double> result = m_metricService.query(query.getParameter());
						Map<String, Map<Long, Double>> line = linecharts.get(linechartId);

						if (line == null) {
							line = new ConcurrentHashMap<String, Map<Long, Double>>();

							linecharts.put(linechartId, line);
						}
						line.put(query.getTitle(), result);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					} finally {
						semaphore.release();
					}
				}
			});
		}

		try {
			semaphore.tryAcquire(queries.size(), 10000, TimeUnit.MILLISECONDS); // 10 seconds timeout
		} catch (InterruptedException e) {
			// ignore it
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return generateLinecharts(linecharts);
	}

	private List<LineChart> generateLinecharts(final Map<String, Map<String, Map<Long, Double>>> linecharts) {
		List<LineChart> results = new LinkedList<LineChart>();

		for (Entry<String, Map<String, Map<Long, Double>>> line : linecharts.entrySet()) {
			String id = line.getKey();
			LineChart linechart = new LineChart();

			linechart.setId(id);
			linechart.setHtmlTitle(id);

			for (Entry<String, Map<Long, Double>> l : line.getValue().entrySet()) {
				linechart.add(l.getKey(), l.getValue());
			}
			results.add(linechart);
		}
		return results;
	}

	public List<String> parseSeries(List<String> measures) {
		List<String> results = new ArrayList<String>();

		for (String measure : measures) {
			measure = measure.replaceAll("(domain=[^,]*(,|$))|(endPoint=[^,]*(,|$))", "").replaceAll(",$", "")
			      .replaceAll(",", ";");

			results.add(measure);
		}
		return results;
	}

}
