/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named
public class IpService implements Initializable {
	private static final String OTHER = "其他";

	private int[] m_areaIds;

	private Map<Integer, Area> m_areas;

	private int[] m_corpIds;

	private Map<Integer, Corporation> m_corps;

	private long[] m_ends;

	private long[] m_starts;

	private int[] m_foreignAreaIds;

	private Map<Integer, Area> m_foreignAreas;

	private long[] m_foreignEnds;

	private long[] m_foreignStarts;

	private String FOREIGN_OTHER = "国外其他";

	private String FOREIGN = "国外";

	private IpInfo buildDefaultIpInfo(String nation, String other) {
		IpInfo ipInfo = new IpInfo();

		ipInfo.setNation(nation);
		ipInfo.setProvince(other);
		ipInfo.setCity(other);
		ipInfo.setChannel(other);
		return ipInfo;
	}

	private IpInfo findChinaIp(long ip) {
		int low = 0, high = m_starts.length - 1, mid;

		while (low <= high) {
			mid = (low + high) / 2;
			if (ip >= m_starts[mid] && ip <= m_ends[mid]) {
				IpInfo ipInfo = new IpInfo();

				Area area = m_areas.get(m_areaIds[mid]);

				if (area != null) {
					ipInfo.setNation(area.getNation());
					ipInfo.setProvince(area.getProvince());
					ipInfo.setCity(area.getCity());
				} else {
					ipInfo.setNation(OTHER);
					ipInfo.setProvince(OTHER);
					ipInfo.setCity(OTHER);
				}

				Corporation corp = m_corps.get(m_corpIds[mid]);

				if (corp != null) {
					ipInfo.setChannel(corp.getName());
				} else {
					ipInfo.setChannel(OTHER);
				}
				return ipInfo;
			} else if (ip < m_starts[mid]) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return null;
	}

	private IpInfo findForeignIp(long ip) {
		int low = 0, high = m_foreignStarts.length - 1, mid;

		while (low <= high) {
			mid = (low + high) / 2;
			if (ip >= m_foreignStarts[mid] && ip <= m_foreignEnds[mid]) {
				IpInfo ipInfo = new IpInfo();
				Area area = m_foreignAreas.get(m_foreignAreaIds[mid]);

				if (area != null) {
					ipInfo.setNation(area.getNation());
					ipInfo.setProvince(area.getProvince());
					ipInfo.setCity(area.getCity());
				} else {
					ipInfo.setNation(FOREIGN);
					ipInfo.setProvince(FOREIGN_OTHER);
					ipInfo.setCity(FOREIGN_OTHER);
				}
				ipInfo.setChannel(FOREIGN_OTHER);
				return ipInfo;
			} else if (ip < m_foreignStarts[mid]) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return buildDefaultIpInfo(FOREIGN, FOREIGN_OTHER);
	}

	private IpInfo findIpInfo(long ip) {
		IpInfo ipInfo = findChinaIp(ip);

		if (ipInfo == null) {
			ipInfo = findForeignIp(ip);
		}
		return ipInfo;
	}

	public IpInfo findIpInfoByString(String ip) {
		try {
			String[] segments = ip.split("\\.");
			if (segments.length != 4) {
				return null;
			}

			long ip_num = Long.parseLong(segments[0]) << 24;
			ip_num += Integer.parseInt(segments[1]) << 16;
			ip_num += Integer.parseInt(segments[2]) << 8;
			ip_num += Integer.parseInt(segments[3]);

			return findIpInfo(ip_num);
		} catch (Exception e) {
			return null;
		}
	}

	private void initAreaMap(InputStream areaFile) {
		try {
			BufferedReader areaReader = new BufferedReader(new InputStreamReader(areaFile));
			String line;
			String[] strs;
			int id;
			m_areas = new LinkedHashMap<Integer, Area>();

			while ((line = areaReader.readLine()) != null) {
				strs = line.split(":");
				id = Integer.parseInt(strs[1]);

				String[] areaStr = strs[0].split("\\|");
				Area area = new Area();
				area.setAreaId(id);
				area.setNation(areaStr[0]);
				area.setProvince(areaStr[1]);
				area.setCity(areaStr[2]);
				m_areas.put(id, area);
			}
			areaReader.close();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void initCorpMap(InputStream corpFile) {
		try {
			BufferedReader corpReader = new BufferedReader(new InputStreamReader(corpFile));
			String line;
			String[] strs;
			int id;
			m_corps = new LinkedHashMap<Integer, Corporation>();

			while ((line = corpReader.readLine()) != null) {
				strs = line.split(":");

				Corporation corp = new Corporation();
				id = Integer.parseInt(strs[1]);
				corp.setCorporationId(id);
				corp.setName(strs[0]);
				m_corps.put(id, corp);
			}
			corpReader.close();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void initForeignAreaMap(InputStream areaFile) {
		try {
			BufferedReader areaReader = new BufferedReader(new InputStreamReader(areaFile));
			String line;
			String[] strs;
			String[] ids;
			m_foreignAreas = new LinkedHashMap<Integer, Area>();

			while ((line = areaReader.readLine()) != null) {
				strs = line.split(":");
				ids = strs[1].split(",");
				Area area = new Area();
				area.setNation("国外");
				area.setProvince(strs[0]);
				area.setCity(strs[0]);

				for (String id : ids) {
					m_foreignAreas.put(Integer.valueOf(id), area);
				}
			}
			areaReader.close();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public void initForeignIpTable(InputStream ipFile) {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(ipFile));
			int size = Integer.parseInt(reader.readLine());
			String line;
			String[] strs;

			m_foreignStarts = new long[size];
			m_foreignEnds = new long[size];
			m_foreignAreaIds = new int[size];
			for (int i = 0; i < size; i++) {
				line = reader.readLine();
				strs = line.split(":");
				m_foreignStarts[i] = Long.parseLong(strs[0]);
				m_foreignEnds[i] = Long.parseLong(strs[1]);
				m_foreignAreaIds[i] = Integer.parseInt(strs[2]);
			}

		} catch (IOException e) {
			Cat.logError(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		InputStream areaFile = IpService.class.getClassLoader().getResourceAsStream("ip/area_china");
		InputStream corpFile = IpService.class.getClassLoader().getResourceAsStream("ip/corp_china");
		InputStream ipFile = IpService.class.getClassLoader().getResourceAsStream("ip/iptable_china");

		initAreaMap(areaFile);
		initCorpMap(corpFile);
		initIpTable(ipFile);

		InputStream foreignAreaFile = IpService.class.getClassLoader().getResourceAsStream("ip/area_foreign");
		InputStream foreignIpFile = IpService.class.getClassLoader().getResourceAsStream("ip/iptable_foreign");

		initForeignAreaMap(foreignAreaFile);
		initForeignIpTable(foreignIpFile);
	}

	public void initIpTable(InputStream ipFile) {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(ipFile));
			int size = Integer.parseInt(reader.readLine());
			String line;
			String[] strs;

			m_starts = new long[size];
			m_ends = new long[size];
			m_areaIds = new int[size];
			m_corpIds = new int[size];
			for (int i = 0; i < size; i++) {
				line = reader.readLine();
				strs = line.split(":");
				m_starts[i] = Long.parseLong(strs[0]);
				m_ends[i] = Long.parseLong(strs[1]);
				m_areaIds[i] = Integer.parseInt(strs[2]);
				m_corpIds[i] = Integer.parseInt(strs[3]);
			}

		} catch (IOException e) {
			Cat.logError(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	public static class Area {
		private Integer m_areaId;

		private String m_city;

		private String m_nation;

		private String m_province;

		public Integer getAreaId() {
			return m_areaId;
		}

		public void setAreaId(Integer areaId) {
			m_areaId = areaId;
		}

		public String getCity() {
			return m_city;
		}

		public void setCity(String city) {
			m_city = city;
		}

		public String getNation() {
			return m_nation;
		}

		public void setNation(String nation) {
			m_nation = nation;
		}

		public String getProvince() {
			return m_province;
		}

		public void setProvince(String province) {
			m_province = province;
		}

	}

	public static class Corporation {
		private Integer m_corporationId;

		private String m_name;

		public Integer getCorporationId() {
			return m_corporationId;
		}

		public void setCorporationId(Integer corporationId) {
			m_corporationId = corporationId;
		}

		public String getName() {
			return m_name;
		}

		public void setName(String name) {
			m_name = name;
		}

	}

	public static class IpInfo {
		private String m_channel;

		private String m_city;

		private String m_nation;

		private String m_province;

		private String m_longitude;

		private String m_latitude;

		public String getChannel() {
			return m_channel;
		}

		public void setChannel(String name) {
			m_channel = name;
		}

		public String getCity() {
			return m_city;
		}

		public void setCity(String city) {
			m_city = city;
		}

		public String getNation() {
			return m_nation;
		}

		public void setNation(String nation) {
			m_nation = nation;
		}

		public String getProvince() {
			return m_province;
		}

		public void setProvince(String province) {
			m_province = province;
		}

		public String getLongitude() {
			return m_longitude;
		}

		public void setLongitude(String longitude) {
			m_longitude = longitude;
		}

		public String getLatitude() {
			return m_latitude;
		}

		public void setLatitude(String latitude) {
			m_latitude = latitude;
		}

	}

}
