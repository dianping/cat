package com.dianping.cat.agent.monitor.executors.jvm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.CommandUtils;

public class TomcatPidManager {

	@Inject
	private CommandUtils m_commandUtils;

	private Set<String> findPidByLocalWay() {
		Set<String> pids = new HashSet<String>();
		String cmd = "ps -ef | grep java | grep tomcat | grep 'catalina.startup.Bootstrap start' | grep -v grep | awk '{print $2}' | tr '\n' ' '";

		try {
			List<String> lines = m_commandUtils.runShell(cmd);
			if (!lines.isEmpty()) {
				String line = lines.iterator().next();
				String[] outputs = line.trim().split(" +");

				for (String pid : outputs) {
					pids.add(pid);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return pids;
	}

	public Set<String> findPidOfTomcat() {
		Set<String> pids = new HashSet<String>();

		try {
			String cmd = "/etc/init.d/tomcat status";
			List<String> outputs = m_commandUtils.runShell(cmd); // The process of tomcat ( pid=9923 ) is running...

			if (!outputs.isEmpty()) {
				Iterator<String> iterator = outputs.iterator();
				String output = iterator.next();

				if (output.contains("running")) {
					String endOutput = output.split("(")[1];
					String pidOutput = endOutput.split("")[0].trim();
					String pid = pidOutput.split("=")[1];

					Integer.parseInt(pid);
					pids.add(pid);
				} else {
					Cat.logError(new RuntimeException("No tomcat running on machine, [ " + cmd + " ] output: " + output));
				}
			} else {
				pids = findPidByLocalWay();
			}
		} catch (Exception e) {
			pids = findPidByLocalWay();
		}
		return pids;
	}
}