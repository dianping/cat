package com.dianping.cat.report.page.server.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricScreen;
import com.dianping.cat.home.dal.report.MetricScreenDao;
import com.dianping.cat.home.dal.report.MetricScreenEntity;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;
import com.dianping.cat.report.page.server.display.MetricScreenTransformer;

@Named
public class MetricScreenService implements Initializable {

	@Inject
	private MetricScreenDao m_dao;

	@Inject
	private MetricGraphBuilder m_graphBuilder;

	@Inject
	private MetricScreenTransformer m_transformer;

	private Map<String, Map<String, MetricScreenInfo>> m_cachedScreens = new ConcurrentHashMap<String, Map<String, MetricScreenInfo>>();

	private MetricScreenInfo buildMetricScreenInfo(GraphParam param) {
		MetricScreenInfo screenInfo = new MetricScreenInfo();

		screenInfo.setName(param.getName());
		screenInfo.setGraphName(param.getGraphName());
		screenInfo.setView(param.getView());
		screenInfo.setEndPoints(param.getEndPoints());
		screenInfo.setMeasures(param.getMeasurements());
		screenInfo.setGraph(m_graphBuilder.buildGraph(param.getEndPoints(), param.getMeasurements(), param.getName()
		      + "-" + param.getGraphName(), param.getView()));
		return screenInfo;
	}

	public void deleteByScreen(String screen) {
		MetricScreen metricScreen = new MetricScreen();

		metricScreen.setName(screen);

		try {
			m_dao.deleteByName(metricScreen);
			m_cachedScreens.remove(screen);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		refresh();
	}

	public void updateScreen(String screen, List<String> graphNames) {
		Map<String, MetricScreenInfo> screens = m_cachedScreens.get(screen);

		if (screens == null) {
			screens = new LinkedHashMap<String, MetricScreenInfo>();

			m_cachedScreens.put(screen, screens);
		}
		insertScreen(screen, graphNames, screens);
		deleteScreen(screen, graphNames, screens);
	}

	private void insertScreen(String screen, List<String> graphNames, Map<String, MetricScreenInfo> screens) {
		for (String graphName : graphNames) {
			MetricScreenInfo metricInfo = screens.get(graphName);

			if (metricInfo == null) {
				metricInfo = new MetricScreenInfo();

				metricInfo.setName(screen).setGraphName(graphName);
				screens.put(graphName, metricInfo);
			}
		}
	}

	private void deleteScreen(String screen, List<String> graphNames, Map<String, MetricScreenInfo> screens) {
		for (String key : screens.keySet()) {
			if (!graphNames.contains(key)) {
				MetricScreen metricScreen = new MetricScreen();

				metricScreen.setName(screen).setGraphName(key);
				try {
					m_dao.deleteByNameGraph(metricScreen);
					screens.remove(key);
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		}
	}

	public void insertOrUpdateGraph(GraphParam param) {
		MetricScreenInfo screenInfo = buildMetricScreenInfo(param);
		MetricScreen entity = m_transformer.transformToMetricScreen(screenInfo);

		try {
			m_dao.insertOrUpdateByNameGraph(entity);

			Map<String, MetricScreenInfo> screens = m_cachedScreens.get(param.getName());
			screens.put(param.getGraphName(), screenInfo);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public Map<String, MetricScreenInfo> queryByName(String name) {
		Map<String, MetricScreenInfo> screeInfos = m_cachedScreens.get(name);

		if (screeInfos == null) {
			screeInfos = new HashMap<String, MetricScreenInfo>();
		}
		return screeInfos;
	}

	public MetricScreenInfo queryByNameGraph(String name, String graphName) {
		Map<String, MetricScreenInfo> screens = m_cachedScreens.get(name);

		if (screens != null) {
			MetricScreenInfo metricScreenInfo = screens.get(graphName);

			return metricScreenInfo;
		}
		return null;
	}

	public Map<String, Map<String, MetricScreenInfo>> queryScreens() {
		return m_cachedScreens;
	}

	private void refresh() {
		try {
			Map<String, Map<String, MetricScreenInfo>> cachedScreens = new ConcurrentHashMap<String, Map<String, MetricScreenInfo>>();
			List<MetricScreen> entities = m_dao.findAll(MetricScreenEntity.READSET_FULL);

			for (MetricScreen entity : entities) {
				String screenName = entity.getName();
				Map<String, MetricScreenInfo> screens = cachedScreens.get(screenName);

				if (screens == null) {
					screens = new LinkedHashMap<String, MetricScreenInfo>();

					cachedScreens.put(screenName, screens);
				}
				screens.put(entity.getGraphName(), m_transformer.transformToScreenInfo(entity));
			}

			m_cachedScreens = cachedScreens;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
}
