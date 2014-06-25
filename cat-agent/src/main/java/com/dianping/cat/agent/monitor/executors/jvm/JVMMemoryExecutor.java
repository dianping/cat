package com.dianping.cat.agent.monitor.executors.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.DataEntity;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;

public class JVMMemoryExecutor extends AbstractExecutor {

	public static final String ID = "JVMMemoryExecutor";

	public static String findPidOfTomcat() {
		String pid = null;
		BufferedReader reader = null;

		try {
			String cmd = "/etc/init.d/tomcat status";
			Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output = reader.readLine(); // The process of tomcat ( pid=9923 ) is running...

			if (output != null) {
				if (output.contains("running")) {
					String endOutput = output.split("(")[1];
					String pidOutput = endOutput.split("")[0].trim();
					pid = pidOutput.split("=")[1];

					Integer.parseInt(pid);
				} else {
					Cat.logError(new RuntimeException("No tomcat running on machine, [ " + cmd + " ] output: " + output));
				}
			} else {
				pid = findPidByLocalWay();
			}
		} catch (Exception e) {
			pid = findPidByLocalWay();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Cat.logError(e);
				}
			}
		}
		return pid;
	}

	public static String findPidByLocalWay() {
		String pid = null;
		BufferedReader reader = null;

		try {
			String cmd = "ps aux | grep java | grep tomcat | grep 'catalina.startup.Bootstrap start' | grep -v grep";
			Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output = reader.readLine();

			if (output != null) {
				String[] outputs = output.split(" +");
				pid = outputs[1];
				String out = reader.readLine();

				if (out != null) {
					String o = null;
					StringBuilder sb = new StringBuilder();
					sb.append(output).append("\n").append(out).append("\n");

					while ((o = reader.readLine()) != null) {
						sb.append(o).append("\n");
					}
					Cat.logError(new RuntimeException("Fetch tomcat pid: [ " + pid
					      + " ], but more than one tomcat is running on machine, [ " + cmd + " ] output: \n" + sb.toString()));
				}
			} else {
				Cat.logError(new RuntimeException("No tomcat running on machine, [ " + cmd + " ]  no output"));
			}
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Cat.logError(e);
				}
			}
		}
		return pid;
	}

	private List<DataEntity> buildJVMMemoryInfo() {
		List<DataEntity> entities = new ArrayList<DataEntity>();
		BufferedReader reader = null;
		String pid = findPidOfTomcat();

		if (pid != null) {
			try {
				Process process = null;

				try {
					process = Runtime.getRuntime().exec("/usr/local/jdk/bin/jstat -gcutil " + pid);
				} catch (Exception e) {
					try {
						process = Runtime.getRuntime().exec("jstat -gcutil " + pid);
					} catch (Exception cause) {
						Cat.logError("Maybe cat agent doesn't know path of jstat ", cause);
					}
				}
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
			} catch (Exception e) {
				Cat.logError(e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						Cat.logError(e);
					}
				}
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
