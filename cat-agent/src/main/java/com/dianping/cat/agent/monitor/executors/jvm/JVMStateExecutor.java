package com.dianping.cat.agent.monitor.executors.jvm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.agent.monitor.DataEntity;

public class JVMStateExecutor extends AbstractJVMExecutor implements Initializable {

	public static final String ID = "JVMStateExecutor";

	private static final String CATALINA_PATH = "/data/applogs/tomcat/catalina.out";

	@Override
	public List<DataEntity> execute() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		entities.addAll(buildCatalinaLogInfo());
		entities.addAll(buildTomcatLiveInfo());

		return entities;
	}

	public List<DataEntity> buildCatalinaLogInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		File logFile = new File(CATALINA_PATH);

		if (logFile.exists()) {
			double bytes = logFile.length();
			Map<String, Double> values = new HashMap<String, Double>();

			values.put(JVM_TYPE + "_catalinaLogSize_" + m_envConfig.getIp(), bytes);
			entities.addAll(buildEntities(values, AVG_TYPE));
		}
		return entities;
	}

	public List<DataEntity> buildTomcatLiveInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		Set<String> currentPids = findPidOfTomcat();
		Map<String, Double> values = new HashMap<String, Double>();

		for (String pid : m_pidsOfTomcat) {
			double value = 0;

			if (currentPids.contains(pid)) {
				value = 1;
			}
			values.put(buildJVMId("tomcatLive", pid), value);
			entities.addAll(buildEntities(values, AVG_TYPE));
		}
		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_pidsOfTomcat.isEmpty()) {
			m_pidsOfTomcat.addAll(findPidOfTomcat());
		}
	}
}
