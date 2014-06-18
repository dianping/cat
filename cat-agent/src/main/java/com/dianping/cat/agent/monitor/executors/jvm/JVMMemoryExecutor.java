package com.dianping.cat.agent.monitor.executors.jvm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;
import com.dianping.cat.agent.monitor.executors.DataEntity;

public class JVMMemoryExecutor extends AbstractExecutor {

	public static final String ID = "JVMMemoryExecutor";

	public static String findPidOfTomcat() {
		String pid = null;

		try {
			Process process = Runtime.getRuntime().exec(
			      new String[] { "/bin/sh", "-c", "ps aux | grep tomcat | grep -v grep" });
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output = reader.readLine();

			if (output != null) {
				if (reader.readLine() != null) {
					Cat.logError(new RuntimeException("More than one tomcat is running"));
				}
				reader.close();
				String[] outputs = output.split(" +");
				pid = outputs[1];
			} else {
				Cat.logError(new RuntimeException("No tomcat is running"));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return pid;
	}

	private List<DataEntity> buildJVMMemoryInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();

		try {
			String pid = findPidOfTomcat();
			Process process = Runtime.getRuntime().exec("jstat -gcutil " + pid);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			reader.readLine();

			String output = reader.readLine();
			String[] metrics = output.split(" +");
			long current = System.currentTimeMillis();

			DataEntity eden = new DataEntity();
			eden.setId(buildJVMDataEntityId("edenUsage")).setType(AVG_TYPE).setTime(current)
			      .setValue(Double.valueOf(metrics[2]) / 100);
			addGroupDomainInfo(eden);
			entities.add(eden);

			DataEntity old = new DataEntity();
			old.setId(buildJVMDataEntityId("oldUsage")).setType(AVG_TYPE).setTime(current)
			      .setValue(Double.valueOf(metrics[3]) / 100);
			addGroupDomainInfo(old);
			entities.add(old);

			DataEntity perm = new DataEntity();
			perm.setId(buildJVMDataEntityId("permUsage")).setType(AVG_TYPE).setTime(current)
			      .setValue(Double.valueOf(metrics[4]) / 100);
			addGroupDomainInfo(perm);
			entities.add(perm);

			return entities;
		} catch (Exception e) {
			Cat.logError(e);
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
