package com.dianping.cat.agent.monitor.puppet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.log4j.Logger;

public class HttpPostUtils {
	private static Logger puppetLogger = Logger.getLogger("myLogger");

	private String urlAddress;

	public String getUrlAddress() {
		return urlAddress;
	}

	public void setUrlAddress(String urlAddress) {
		this.urlAddress = urlAddress;
	}

	public boolean httpPost(String[] params) {
		boolean flag = false;
		URL url = null;
		HttpURLConnection con = null;
		BufferedReader in = null;
		StringBuffer result = new StringBuffer();
		String paramsTemp = "";

		try {
			url = new URL(this.urlAddress);
			con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			for (String param : params) {
				if (param != null && !"".equals(param.trim())) {
					paramsTemp += "&" + param;
				}
			}

			// puppetLogger.info("POST para:"+paramsTemp);

			byte[] b = paramsTemp.getBytes();
			con.getOutputStream().write(b); // to be tested
			con.getOutputStream().flush();
			con.getOutputStream().close();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				} else {
					result.append(line);
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
			// puppetLogger.debug(e);
			puppetLogger.error(e.getMessage(), e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (con != null) {
					con.disconnect();
				}
			} catch (IOException e) {
				puppetLogger.error(e.getMessage(), e);
			}
		}
		String rs = result.toString();
		if (rs.contains("200")) {
			flag = true;
			puppetLogger.info("POST Succ");
		} else {
			flag = false;
			puppetLogger.error("POST Fail:" + paramsTemp);
		}
		return flag;
	}

}
