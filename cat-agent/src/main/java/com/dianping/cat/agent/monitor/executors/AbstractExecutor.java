package com.dianping.cat.agent.monitor.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.agent.monitor.CommandUtils;
import com.dianping.cat.agent.monitor.DataEntity;

public abstract class AbstractExecutor implements Executor {

	@Inject
	protected EnvConfig m_envConfig;
	
	@Inject
	protected CommandUtils m_commandUtils;

	public static final String SUM_TYPE = "sum";

	public static final String AVG_TYPE = "avg";

	public static final String COUNT_TYPE = "count";

	public static final String JVM_TYPE = "jvm";

	public static final String SYSTEM_TYPE = "system";

	public static final String NGINX_TYPE = "nginx";

	protected void addGroupDomainInfo(DataEntity entity) {
		entity.setGroup(m_envConfig.getGroup()).setDomain(m_envConfig.getDomain());
	}

	protected List<DataEntity> buildEntities(Map<String, Double> values, String type) {
		ArrayList<DataEntity> entities = new ArrayList<DataEntity>();
		long time = System.currentTimeMillis();

		for (Entry<String, Double> entry : values.entrySet()) {
			DataEntity entity = new DataEntity();
			String key = entry.getKey();
			double value = entry.getValue();

			entity.setId(key).setType(type).setTime(time).setValue(value);
			addGroupDomainInfo(entity);
			entities.add(entity);
		}
		return entities;
	}

	protected String buildJVMId(String id, String pid) {
		return JVM_TYPE + "_" + id + "@" + pid + "_" + m_envConfig.getIp();
	}

	protected String buildNginxId(String id) {
		return NGINX_TYPE + "_" + id + "_" + m_envConfig.getIp();
	}
	
	protected String buildSystemId(String id) {
		return SYSTEM_TYPE + "_" + id + "_" + m_envConfig.getIp();
	}

}
