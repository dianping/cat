package com.dianping.cat.agent.monitor.jvm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.agent.monitor.AbstractExecutor;
import com.dianping.cat.agent.monitor.DataEntity;

public class JVMStateExecutor extends AbstractExecutor {

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

			entity.setId(buildJVMDataEntityId("catalinaLogSize")).setType(SUM_TYPE).setValue(kilobytes);
			entities.add(entity);
		}
		return entities;
	}

	public List<DataEntity> buildTomcatLiveInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		DataEntity entity = new DataEntity();
		String pid = JVMMemoryExecutor.findPidOfTomcat();
		entity.setId(buildJVMDataEntityId("tomcatLive")).setType(AVG_TYPE);
		
		if (pid == null) {
			entity.setValue(0);
		} else {
			entity.setValue(1);
		}
		entities.add(entity);
		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}

}
