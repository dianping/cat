package com.dianping.cat.agent.monitor.executors.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.executors.AbstractExecutor;
import com.dianping.cat.agent.monitor.executors.DataEntity;

public class JVMMemoryExecutor extends AbstractExecutor {

	public static final String ID = "JVMMemoryExecutor";

	public static boolean checkSingleTomcat() {
		boolean result = false;
		BufferedReader reader = null;

		try {
			Process process = Runtime.getRuntime().exec(
			      new String[] { "/bin/sh", "-c", "ps aux | grep tomcat | grep -v grep  | wc -l" });
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output = reader.readLine();

			if (output != null) {
				String outputs[] = output.split(" +");
				int length = outputs.length;
				int n = 0;

				if (length == 1) {
					n = Integer.parseInt(outputs[0]);
				} else if (length > 1) {
					n = Integer.parseInt(outputs[1]);
				}

				if (n == 1) {
					result = true;
				} else if (n > 1) {
					Cat.logError(new RuntimeException("More than one tomcat is running on machine"));
				} else if (n == 0) {
					Cat.logError(new RuntimeException("No tomcat is running on machine"));
				}
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
		return result;
	}

	public static String findPidOfTomcat() {
		String pid = null;
		BufferedReader reader = null;

		try {
			if (checkSingleTomcat()) {
				Process process = Runtime.getRuntime().exec(
				      new String[] { "/bin/sh", "-c", "ps aux | grep tomcat | grep -v grep" });
				reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String output = reader.readLine();

				if (output != null) {
					String out = reader.readLine();
					if (out != null) {
						Cat.logError(new RuntimeException("[ ps aux | grep tomcat | grep -v grep ] 2nd line output: " + out));
					}
					String[] outputs = output.split(" +");
					pid = outputs[1];
				} else {
					Cat.logError(new RuntimeException("[ ps aux | grep tomcat | grep -v grep ] 1st line no output"));
				}
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
