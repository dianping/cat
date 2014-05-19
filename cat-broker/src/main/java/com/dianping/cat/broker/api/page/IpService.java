package com.dianping.cat.broker.api.page;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;

public class IpService implements Initializable {
	private Map<Integer, Area> m_areas;

	private Map<Integer, Corporation> m_corps;

	private int[] m_areaIds;

	private int[] m_corpIds;

	private long[] m_ends;

	private long[] m_starts;

	private IpInfo findIpInfo(long ip) {
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
					ipInfo.setNation("未知");
					ipInfo.setProvince("未知");
					ipInfo.setCity("未知");
				}

				Corporation corp = m_corps.get(m_corpIds[mid]);

				if (corp != null) {
					ipInfo.setChannel(corp.getName());
				} else {
					ipInfo.setChannel("其他");
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

	public IpInfo findIpInfoByString(String ip) {
		String[] segments = ip.split("\\.");
		if (segments.length != 4) {
			return null;
		}

		try {
			long ip_num = Long.parseLong(segments[0]) << 24;
			ip_num += Integer.parseInt(segments[1]) << 16;
			ip_num += Integer.parseInt(segments[2]) << 8;
			ip_num += Integer.parseInt(segments[3]);

			return findIpInfo(ip_num);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void initAreaMap(String areaFile) {
		try {
			BufferedReader areaReader = new BufferedReader(new InputStreamReader(new FileInputStream(areaFile)));
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

	private void initCorpMap(String corpFile) {
		try {
			BufferedReader corpReader = new BufferedReader(new InputStreamReader(new FileInputStream(corpFile)));
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

	@Override
	public void initialize() throws InitializationException {
		String areaFile = IpService.class.getResource("/config/area_china").getFile();
		String corpFile = IpService.class.getResource("/config/corp_china").getFile();
		String ipFile = IpService.class.getResource("/config/iptable_china").getFile();

		initAreaMap(areaFile);
		initCorpMap(corpFile);
		initIpTable(ipFile);
	}

	public void initIpTable(String ipFile) {
		DataInputStream reader = null;
		try {
			RandomAccessFile sizeReader = new RandomAccessFile(ipFile, "r");
			sizeReader.seek(sizeReader.length() - 4);
			int size = sizeReader.readInt();
			sizeReader.close();

			reader = new DataInputStream(new FileInputStream(ipFile));
			m_starts = new long[size];
			m_ends = new long[size];
			m_areaIds = new int[size];
			m_corpIds = new int[size];
			for (int i = 0; i < size; i++) {
				m_starts[i] = reader.readLong();
				m_ends[i] = reader.readLong();
				m_areaIds[i] = reader.readInt();
				m_corpIds[i] = reader.readInt();
			}
		} catch (IOException e) {
			Cat.logError(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
		}
	}

	public static class Area {
		private Integer m_areaId;

		private String m_nation;

		private String m_province;

		private String m_city;

		public Integer getAreaId() {
			return m_areaId;
		}

		public String getCity() {
			return m_city;
		}

		public String getNation() {
			return m_nation;
		}

		public String getProvince() {
			return m_province;
		}

		public void setAreaId(Integer areaId) {
			m_areaId = areaId;
		}

		public void setCity(String city) {
			m_city = city;
		}

		public void setNation(String nation) {
			m_nation = nation;
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

		public String getName() {
			return m_name;
		}

		public void setCorporationId(Integer corporationId) {
			m_corporationId = corporationId;
		}

		public void setName(String name) {
			m_name = name;
		}

	}

	public static class IpInfo {
		private String m_nation;

		private String m_province;

		private String m_city;

		private String m_channel;

		public String getChannel() {
			return m_channel;
		}

		public String getCity() {
			return m_city;
		}

		public String getNation() {
			return m_nation;
		}

		public String getProvince() {
			return m_province;
		}

		public void setChannel(String name) {
			m_channel = name;
		}

		public void setCity(String city) {
			m_city = city;
		}

		public void setNation(String nation) {
			m_nation = nation;
		}

		public void setProvince(String province) {
			m_province = province;
		}
	}

}
