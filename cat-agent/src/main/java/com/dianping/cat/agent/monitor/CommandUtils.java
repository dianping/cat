package com.dianping.cat.agent.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;

public class CommandUtils {

	public List<String> runShell(String cmd) throws Exception {
		List<String> result = new LinkedList<String>();
		InputStreamReader sReader = null;
		BufferedReader bReader = null;
		Process process = null;

		try {
			process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
			sReader = new InputStreamReader(process.getInputStream());
			bReader = new BufferedReader(sReader);
			String line = null;

			while ((line = bReader.readLine()) != null && StringUtils.isNotEmpty(line)) {
				result.add(line);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (sReader != null) {
					sReader.close();
				}
				if (bReader != null) {
					bReader.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				throw e;
			}
		}
		return result;
	}
}
