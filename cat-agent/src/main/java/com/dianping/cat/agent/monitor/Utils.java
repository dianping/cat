package com.dianping.cat.agent.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;

public class Utils {

	public static List<String> runShell(String cmd) throws Exception {
		List<String> result = new LinkedList<String>();
		BufferedReader reader = null;

		try {
			Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while ((line = reader.readLine()) != null && StringUtils.isNotEmpty(line)) {
				result.add(line);
			}

		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new Exception(e);
				}
			}
		}
		return result;
	}
}
