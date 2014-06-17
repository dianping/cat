package com.dianping.cat.agent.monitor.puppet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.EnvironmentConfig;
import com.dianping.cat.message.Event;

public class DataSender {

	private EnvironmentConfig m_environmentConfig;

	public DataSender(EnvironmentConfig config) {
		m_environmentConfig = config;
	}

	public void send(Alteration alertation) {
		String[] pars = new String[11];
		pars[0] = "type=" + alertation.getType();
		pars[1] = "title=" + alertation.getTitle();
		pars[2] = "domain=" + alertation.getDomain();
		pars[3] = "ip=" + alertation.getIp();
		pars[4] = "user=" + alertation.getUser();
		pars[5] = "content=" + alertation.getContent();
		pars[6] = "url=" + alertation.getUrl();
		pars[7] = "op=" + alertation.getOp();
		pars[8] = "alterationDate=" + alertation.getDate();
		pars[9] = "hostname=" + alertation.getHostname();
		pars[10] = "group=" + alertation.getGroup();

		for (String server : m_environmentConfig.getServers()) {
			String url = m_environmentConfig.buildAlterationUrl(server);
			if (postData(pars, url)) {
				break;
			}
		}
	}

	public boolean postData(String[] params, String urlStr) {
		URL url = null;
		HttpURLConnection con = null;
		String result = null;
		String paramsTemp = "";

		try {
			url = new URL(urlStr);

			con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			for (String param : params) {
				if (param != null && !"".equals(param.trim())) {
					paramsTemp += "&" + param;
				}
			}

			byte[] b = paramsTemp.getBytes();

			con.getOutputStream().write(b); // to be tested
			con.getOutputStream().flush();
			con.getOutputStream().close();

			result = Files.forIO().readFrom(con.getInputStream(), "utf-8");
		} catch (IOException e) {
			Cat.logError(e);
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		boolean flag = false;

		if (result.contains("200")) {
			flag = true;
		} else {
			Cat.logEvent("Puppet", "Failed in posting data", Event.SUCCESS, result);
		}
		return flag;
	}

}
