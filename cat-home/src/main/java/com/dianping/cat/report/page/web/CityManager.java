package com.dianping.cat.report.page.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.JsonBuilder;

public class CityManager implements Initializable {

	public Map<String, List<City>> m_cities = new LinkedHashMap<String, List<City>>();

	public Map<String, List<City>> getCities() {
		return m_cities;
	}

	public String getCityInfo() {
		return new JsonBuilder().toJson(m_cities);
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			loadChinaCities();
			loadForeignCities();
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	private void loadChinaCities() throws IOException {
		String content = Files.forIO().readFrom(this.getClass().getResourceAsStream("/config/city"), "utf-8");
		String[] cities = content.split("\n");

		for (String temp : cities) {
			String[] tabs = temp.split("\\|");

			if (tabs.length > 3) {
				String province = tabs[1];
				String city = tabs[2];

				List<City> list = m_cities.get(province);

				if (list == null) {
					list = new ArrayList<City>();

					if (province.length() > 0) {
						list.add(new City(province, ""));
					}
					m_cities.put(province, list);
				}
				list.add(new City(province, city));
			}
		}
	}

	private void loadForeignCities() throws IOException {
		String foreginContent = Files.forIO().readFrom(this.getClass().getResourceAsStream("/config/city_foreign"),
		      "utf-8");
		String[] foreignCities = foreginContent.split("\n");

		for (String city : foreignCities) {
			String province = city;
			List<City> list = m_cities.get(province);

			if (list == null) {
				list = new ArrayList<City>();

				if (province.length() > 0) {
					list.add(new City(province, ""));
				}
				m_cities.put(province, list);
			}
			list.add(new City(province, city));
		}

		String foreign = "国外其他";
		List<City> foreigns = m_cities.get(foreign);

		if (foreigns == null) {
			foreigns = new ArrayList<City>();

			foreigns.add(new City(foreign, ""));
			foreigns.add(new City(foreign, foreign));
			m_cities.put(foreign, foreigns);
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
