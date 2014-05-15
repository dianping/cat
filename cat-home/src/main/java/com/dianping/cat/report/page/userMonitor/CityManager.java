package com.dianping.cat.report.page.userMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;

public class CityManager implements Initializable {

	public List<String> m_cities = new ArrayList<String>();

	@Override
	public void initialize() throws InitializationException {
		try {
			String content = Files.forIO().readFrom(this.getClass().getResourceAsStream("/config/china"), "utf-8");
			String[] cities = content.split("\n");

			for (String city : cities) {
				m_cities.add(city);
			}
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	public List<String> getCities() {
		return m_cities;
	}
}
