package com.dianping.cat.agent.monitor.executors.jvm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;

public class JVMStateExecutor extends AbstractExecutor {

	public static final String ID = "JVMStateExecutor";

	public List<DataEntity> buildCatalinaLogInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		File logFile = new File(m_envConfig.getCatalinaPath());

		if (logFile.exists()) {
			double bytes = logFile.length();
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(JVM_TYPE + "_catalinaLogSize_" + m_envConfig.getIp(), bytes);
			entities.addAll(buildEntities(values, AVG_TYPE));
		}
		return entities;
	}

	@Override
	public List<DataEntity> execute() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		entities.addAll(buildCatalinaLogInfo());
		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}
}