package com.dianping.cat.report.page.userMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.report.page.JsonBuilder;

public class CityManager implements Initializable {

	public Map<String, List<City>> maps = new LinkedHashMap<String, List<City>>();

	public String getCityInfo() {
		return new JsonBuilder().toJson(maps);
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			String content = Files.forIO().readFrom(this.getClass().getResourceAsStream("/config/city"), "utf-8");
			String[] cities = content.split("\n");

			for (String temp : cities) {
				String[] tabs = temp.split("\\|");

				if (tabs.length > 3) {
					String province = tabs[1];
					String city = tabs[2];

					List<City> list = maps.get(province);

					if (list == null) {
						list = new ArrayList<City>();
						list.add(new City(province, ""));

						maps.put(province, list);
					}
					list.add(new City(province, city));
				}
			}
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	public class City {

		private String m_province;

		private String m_city;

		public City(String province, String city) {
			m_province = province;
			m_city = city;
		}

		public String getCity() {
			return m_city;
		}

		public String getProvince() {
			return m_province;
		}

		public void setCity(String city) {
			m_city = city;
		}

		public void setProvince(String province) {
			m_province = province;
		}
	}
}
