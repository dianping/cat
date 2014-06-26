package com.dianping.cat.agent.monitor.executors.jvm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
			double kilobytes = (bytes / 1024);
			DataEntity entity = new DataEntity();

			entity.setId(JVM_TYPE + "_catalinaLogSize_" + m_envConfig.getIp()).setType(SUM_TYPE)
			      .setTime(System.currentTimeMillis()).setValue(kilobytes);
			addGroupDomainInfo(entity);
			entities.add(entity);
		}
		return entities;
	}

	public List<DataEntity> buildTomcatLiveInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		Set<String> currentPids = findPidOfTomcat();
		long current = System.currentTimeMillis();

		for (String pid : m_pidsOfTomcat) {
			DataEntity entity = new DataEntity();

			entity.setId(buildJVMDataEntityId("tomcatLive", pid)).setType(AVG_TYPE).setTime(current);
			addGroupDomainInfo(entity);

			if (currentPids.contains(pid)) {
				entity.setValue(1);
			} else {
				entity.setValue(0);
			}
			entities.add(entity);
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
