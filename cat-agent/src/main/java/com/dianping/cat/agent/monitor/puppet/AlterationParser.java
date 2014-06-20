package com.dianping.cat.agent.monitor.puppet;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import com.dianping.cat.Cat;
import com.dianping.cat.agent.monitor.EnvironmentConfig;

public class AlterationParser {

	private EnvironmentConfig m_environmentConfig;

	public AlterationParser(EnvironmentConfig config) {
		m_environmentConfig = config;
	}

	public Alteration parse(String line) {
		String title_add = "";
		String group = "puppet";
		String type = "puppet";
		String user = "puppet";
		String url = "";
		String op = "insert";
		String date = "";
		String content = "";
		String host = m_environmentConfig.getHostName();
		String IP = m_environmentConfig.getIp();
		String domain = m_environmentConfig.getDomain();
		String title = "puppet";
		String regEx = ".*puppet-agent.*\\(\\/Stage";
		String regEx_Filebucketed = ".*Filebucketed.*";
		String tmp = "";
		Alteration alertation = new Alteration();

		// 'Mar 25 10:56:27 localhost puppet-agent[24773]: (/Stage[main]/Zabbix::Agentd/Exec[restart_zabbix_agentd]) Triggered
		// 'refresh' from 1 events'

		if (Pattern.compile(regEx).matcher(line).find()) {
			String[] tmp_list = line.split(" +");
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);

			if (tmp_list.length >= 4) {
				date = tmp_list[0] + " " + tmp_list[1] + " " + tmp_list[2] + " " + Integer.toString(year);
			}

			String all_content = line.split("\\(")[1]; // "/Stage[main]/Zabbix::Agentd/Exec[restart_zabbix_agentd]) Triggered 'refresh' from 1 events"
			String[] tmp_string = all_content.split("\\)"); // '/Stage[main]/Zabbix::Agentd/Exec[restart_zabbix_agentd]', ''
			String[] tmp_string_main = tmp_string[0].split("\\[main\\]\\/");
			if (tmp_string_main.length >= 2) {
				title = tmp_string_main[1].split("\\[")[1].split("\\]")[0]; // restart_zabbix_agentd
				if (title == "") {
					title = "puppet";
				}
			}
			if (tmp_string.length >= 2) {
				content = all_content.split("\\)")[1]; // " Triggered 'refresh' from 1 events"
				String[] tmpContent = content.split(" ");

				if (tmpContent.length >= 3) {
					title_add = tmpContent[1] + " " + tmpContent[2];
					title = title + " " + title_add; // restart_zabbix_agentd Triggered 'refresh'
				}
			}
			if (Pattern.compile(regEx_Filebucketed).matcher(content).find()) {
				String[] tmpContent = content.split(" ");
				String new_file = tmpContent[2];// "/usr/local/nginx/conf/nginx_app.conf"

				// " Filebucketed /usr/local/nginx/conf/nginx_app.conf to puppet with sum 99c3e5f79645493fdcf4340dd457cbe4"
				if (tmpContent.length >= 8) {
					String old_file_index = tmpContent[7]; // 99c3e5f79645493fdcf4340dd457cbe4
					String old_file_dir = Utils.runSysCmd("find /var/lib/puppet/clientbucket -name " + old_file_index)
					      .toString().split("\n")[0];
					String old_file = old_file_dir + "/contents";

					if (new File(new_file).exists() && new File(old_file).exists()) {
						tmp = Utils.runSysCmd("diff " + old_file + " " + new_file).toString();
					}
					if (tmp.trim() != "") {
						content = tmp;
					}
				}
			}
			SimpleDateFormat sdf_mmm = new SimpleDateFormat("MMMM dd HH:mm:ss yyyy", Locale.US);
			SimpleDateFormat sdf_normal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

			try {
				date = sdf_normal.format(sdf_mmm.parse(date));
			} catch (ParseException e1) {
				Cat.logError(e1);
				sdf_mmm = new SimpleDateFormat("MMMM  dd HH:mm:ss yyyy", Locale.US);
				try {
					date = sdf_normal.format(sdf_mmm.parse(date));
				} catch (ParseException e) {
					Cat.logError(e);
				}
				return null;
			}

			alertation.setHostname(host);
			alertation.setIp(IP);
			alertation.setDomain(domain);
			alertation.setTitle(title);
			alertation.setContent(content);
			alertation.setOp(op);
			alertation.setUrl(url);
			alertation.setUser(user);
			alertation.setType(type);
			alertation.setDate(date);
			alertation.setGroup(group);
		} else {
			alertation = null;
		}
		return alertation;
	}

}
