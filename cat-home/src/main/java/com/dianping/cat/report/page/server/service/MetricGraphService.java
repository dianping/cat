package com.dianping.cat.report.page.server.service;

import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricGraph;
import com.dianping.cat.home.dal.report.MetricGraphDao;
import com.dianping.cat.home.dal.report.MetricGraphEntity;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

@Named
public class MetricGraphService implements Initializable {

	@Inject
	private MetricGraphDao m_dao;

	private volatile MetricGraph m_last = new MetricGraph();

	public boolean deleteBeforeDate(Date date) {
		MetricGraph graph = m_dao.createLocal();

		graph.setCreationDate(date);

		try {
			m_dao.deleteBeforeDate(graph);
			return true;
		} catch (DalException e) {
			return false;
		}
	}

	public boolean deleteById(int id) {
		boolean ret = true;
		MetricGraph entity = m_dao.createLocal();

		entity.setId(id);

		try {
			m_dao.deleteByPK(entity);
		} catch (DalException e) {
			ret = false;

			Cat.logError(e);
		}
		return ret;
	}

	public MetricGraph getLast() {
		return m_last;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			m_last = m_dao.findLast(1, MetricGraphEntity.READSET_FULL);
		} catch (DalNotFoundException e) {
			// Ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public boolean insert(Graph graph) {
		boolean ret = true;
		MetricGraph entity = m_dao.createLocal();

		entity.setGraphId(Long.valueOf(graph.getId()));
		entity.setContent(graph.toString());

		try {
			m_dao.insert(entity);

			m_last = entity;
		} catch (DalException e) {
			ret = false;

			Cat.logError(e);
		}

		return ret;
	}

	public Graph queryByGraphId(long id) {
		try {
			MetricGraph entity = m_dao.findByGrapId(id, MetricGraphEntity.READSET_FULL);
			String xml = entity.getContent();
			Graph graph = DefaultSaxParser.parse(xml);

			return graph;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}
}
