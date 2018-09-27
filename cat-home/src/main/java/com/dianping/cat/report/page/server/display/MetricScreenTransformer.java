package com.dianping.cat.report.page.server.display;

import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricScreen;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

@Named
public class MetricScreenTransformer {

	public final static String SEPARATOR = ",";

	public MetricScreen transformToMetricScreen(MetricScreenInfo screenInfo) {
		MetricScreen metricScreen = new MetricScreen();

		metricScreen.setName(screenInfo.getName());
		metricScreen.setGraphName(screenInfo.getGraphName());
		metricScreen.setView(screenInfo.getView());
		metricScreen.setEndPoints(StringUtils.join(screenInfo.getEndPoints(), SEPARATOR));
		metricScreen.setMeasurements(StringUtils.join(screenInfo.getMeasures(), SEPARATOR));
		metricScreen.setContent(screenInfo.getGraph().toString());

		return metricScreen;
	}

	public MetricScreenInfo transformToScreenInfo(MetricScreen entity) {
		MetricScreenInfo metricScreenInfo = new MetricScreenInfo();
		List<String> endPoints = Splitters.by(SEPARATOR).noEmptyItem().split(entity.getEndPoints());
		List<String> measures = Splitters.by(SEPARATOR).noEmptyItem().split(entity.getMeasurements());

		metricScreenInfo.setName(entity.getName()).setGraphName(entity.getGraphName()).setView(entity.getView())
		      .setMeasures(measures).setEndPoints(endPoints);

		try {
			Graph graph = DefaultSaxParser.parse(entity.getContent());

			metricScreenInfo.setGraph(graph);
			return metricScreenInfo;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}
}
