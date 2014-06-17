package com.dianping.cat.agent.monitor.puppet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;

public class Utils {

	public static StringBuffer runSysCmd(String cmd) {
		String regEx = "^chown.*|^diff.*|^find.*";

		StringBuffer result = new StringBuffer();
		Runtime run = Runtime.getRuntime();
		Process p = null;

		if (!Pattern.compile(regEx).matcher(cmd).find()) {
			Cat.logEvent("Puppet", "runSysCms failed", Event.SUCCESS, "不支持命令：" + cmd);
			return null;
		}
		try {
			p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null) {
				result.append(lineStr + "\n");
			}
			if (p.waitFor() != 0) {
				if (p.exitValue() != 0)// p.exitValue()==0表示正常结束，1：非正常结束
					Cat.logEvent("Puppet", "命令执行失败?: " + cmd);
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			if (p != null) {
				try {
					p.getOutputStream().close();
					p.getInputStream().close();
					p.getErrorStream().close();
				} catch (IOException e) {
					Cat.logError(e);
				}
			}
		}
		return result;

	}
}
