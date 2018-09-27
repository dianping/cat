package com.dianping.cat.alarm.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.server.QueryParameter;
import com.dianping.cat.server.ServerGroupByEntity;

@Named(type = ServerAlarmTask.class, instantiationStrategy = Named.PER_LOOKUP)
public class ServerAlarmTask extends ContainerHolder implements Task {

	@Inject
	private MetricService m_metricService;

	@Inject
	private ServerDataChecker m_dataChecker;

	@Inject
	protected AlertManager m_sendManager;

	private List<AlarmParameter> m_paramters = new ArrayList<AlarmParameter>();

	private String m_category;

	private String m_alarmId;

	public void addParameter(AlarmParameter parameter) {
		m_paramters.add(parameter);
	}

	public String getAlarmId() {
		return m_alarmId;
	}

	public String getCategory() {
		return m_category;
	}

	@Override
	public String getName() {
		return "alarm-task";
	}

	public List<AlarmParameter> getParamters() {
		return m_paramters;
	}

	@Override
	public void run() {
		for (AlarmParameter parameter : m_paramters) {
			List<Condition> conditions = parameter.getConditions();
			QueryParameter query = parameter.getQuery();
			List<ServerGroupByEntity> entities = m_metricService.queryByFields(query);

			for (ServerGroupByEntity e : entities) {
				Map<Long, Double> results = e.getValues();

				if (!results.isEmpty()) {
					SortHelper.sortMap(results, new Comparator<Entry<Long, Double>>() {
						@Override
						public int compare(Entry<Long, Double> o1, Entry<Long, Double> o2) {
							if (o1.getKey() > o2.getKey()) {
								return 1;
							} else if (o1.getKey() < o2.getKey()) {
								return -1;
							} else {
								return 0;
							}
						}
					});
					Double[] values = new Double[results.size()];

					results.values().toArray(values);

					List<DataCheckEntity> alertResults = m_dataChecker.checkData(ArrayUtils.toPrimitive(values), conditions);

					for (DataCheckEntity alertResult : alertResults) {
						AlertEntity entity = new AlertEntity();

						entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
						      .setLevel(alertResult.getAlertLevel());
						entity.setMetric(e.getMeasurement()).setType(m_alarmId).setGroup(e.getEndPoint());
						m_sendManager.addAlert(entity);
					}
				}
			}
		}
	}

	public void setAlarmId(String alarmId) {
		m_alarmId = alarmId;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	@Override
	public void shutdown() {
		release(this);
	}

	public static class AlarmParameter {

		private QueryParameter m_query;

		private List<Condition> m_conditions = new ArrayList<Condition>();

		public AlarmParameter(QueryParameter query, List<Condition> conditions) {
			m_query = query;
			m_conditions = conditions;
		}

		public QueryParameter getQuery() {
			return m_query;
		}

		public List<Condition> getConditions() {
			return m_conditions;
		}

	}

}
