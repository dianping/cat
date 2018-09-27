package com.dianping.cat.report.page.server.service;

import java.util.ArrayList;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;
import com.dianping.cat.server.MetricType;

public class MetricGraphBuilder {

	public final static String TAG_SEPARATOR = ";";

	private List<Item> buildEndPointView(List<String> measurements, List<String> endPoints) {
		List<Item> items = new ArrayList<Item>();

		for (String endPoint : endPoints) {
			Item item = new Item(endPoint);

			item.setView("endPoint");

			for (String measure : measurements) {
				Pair<String, String> pair = splitCategoryMeasure(measure);
				String category = pair.getKey();

				if (category != null) {
					String realMeasure = pair.getValue();
					Segment segment = new Segment(measure);

					segment.setCategory(category);
					segment.setEndPoint(endPoint);
					segment.setMeasure(realMeasure);
					segment.setTags(buildTag(measure, endPoint));
					segment.setType(MetricType.AVG.getName());

					item.addSegment(segment);
				}
			}
			items.add(item);
		}
		return items;
	}

	public Graph buildGraph(List<String> endPoints, List<String> measurements, String graphId, String view) {
		List<Item> results = new ArrayList<Item>();
		Graph graph = new Graph(graphId);

		if (StringUtils.isEmpty(view)) {
			results.addAll(buildMeasureView(measurements, endPoints));
			results.addAll(buildEndPointView(measurements, endPoints));
		} else if (Constants.END_POINT.equals(view)) {
			results.addAll(buildEndPointView(measurements, endPoints));
		} else if (Constants.MEASUREMENT.equals(view)) {
			results.addAll(buildMeasureView(measurements, endPoints));
		}

		for (Item item : results) {
			graph.addItem(item);
		}
		return graph;
	}

	private List<Item> buildMeasureView(List<String> measurements, List<String> endPoints) {
		List<Item> items = new ArrayList<Item>();

		for (String measure : measurements) {
			Pair<String, String> pair = splitCategoryMeasure(measure);
			String category = pair.getKey();

			if (category != null) {
				String realMeasure = pair.getValue();
				Item item = new Item(measure);

				item.setView("measurement");

				for (String endPoint : endPoints) {
					Segment segment = new Segment(endPoint);

					segment.setCategory(category);
					segment.setEndPoint(endPoint);
					segment.setMeasure(realMeasure);
					segment.setTags(buildTag(measure, endPoint));
					segment.setType(MetricType.AVG.getName());

					item.addSegment(segment);
					items.add(item);
				}
			}
		}
		return items;
	}

	public String buildTag(String measure, String endPoint) {
		StringBuilder sb = new StringBuilder("endPoint='" + endPoint + "'");
		int index = measure.indexOf(TAG_SEPARATOR);

		if (index > -1) {
			String tags = measure.substring(index + 1);
			List<String> subTags = Splitters.by(TAG_SEPARATOR).noEmptyItem().split(tags);

			for (String s : subTags) {
				try {
					String[] fields = s.split("=");

					sb.append(TAG_SEPARATOR).append(fields[0]).append("='").append(fields[1]).append("'");
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
		return sb.toString();
	}

	private Pair<String, String> splitCategoryMeasure(String measurement) {
		int index = measurement.indexOf(".");
		String category = null;
		String measure = measurement;

		if (index > 0) {
			category = measurement.substring(0, index);
		} else {
			Cat.logError(new RuntimeException("Error metirc format: " + measurement));
		}

		index = measurement.indexOf(TAG_SEPARATOR);

		if (index > 0) {
			measure = measure.substring(0, index);
		}
		return new Pair<String, String>(category, measure);
	}

}
