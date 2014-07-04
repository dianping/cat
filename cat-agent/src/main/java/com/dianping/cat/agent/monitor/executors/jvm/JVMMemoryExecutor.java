package com.dianping.cat.agent.monitor.executors.jvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;

public class JVMMemoryExecutor extends AbstractExecutor {

	@Inject
	private TomcatPidManager m_manager;

	public static final String ID = "JVMMemoryExecutor";

	private List<DataEntity> buildJVMMemoryInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		Set<String> pids = m_manager.findPidOfTomcat();

		for (String pid : pids) {
			List<String> lines = null;

			try {
				lines = m_commandUtils.runShell("/usr/local/jdk/bin/jstat -gcutil " + pid);
			} catch (Exception e) {
				try {
					lines = m_commandUtils.runShell("jstat -gcutil " + pid);
				} catch (Exception cause) {
					Cat.logError("Maybe cat agent doesn't know path of jstat ", cause);
				}
			}
			if (lines.size() == 2) {
				Iterator<String> iterator = lines.iterator();
				iterator.next();
				String line = iterator.next();
				String[] metrics = line.split(" +");

				try {
					Map<String, Double> values = new HashMap<String, Double>();

					values.put(buildJVMId("edenUsage", pid), Double.valueOf(metrics[2]) / 100);
					values.put(buildJVMId("oldUsage", pid), Double.valueOf(metrics[3]) / 100);
					values.put(buildJVMId("permUsage", pid), Double.valueOf(metrics[4]) / 100);
					entities.addAll(buildEntities(values, AVG_TYPE));
				} catch (Exception e) {
					Cat.logError(e);
				}
			} else {
				Cat.logError(new RuntimeException("No tomcat is running, [jstat -gcutil] result: " + lines));
			}
		}
		return entities;
	}

	@Override
	public List<DataEntity> execute() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		entities.addAll(buildJVMMemoryInfo());

		return entities;
	}

	@Override
	public String getId() {
		return ID;
	}
}