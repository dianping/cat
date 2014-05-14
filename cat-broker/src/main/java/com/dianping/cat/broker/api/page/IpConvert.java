package com.dianping.cat.broker.api.page;

public class IpConvert {
	
	public PositionInfo convert(String ip) {
		PositionInfo info = new PositionInfo();

		info.setChannel("liantong");
		info.setCity("shanghai");
		return info;
	}

	public static class PositionInfo {

		private String m_city;

		private String m_channel;

		public String getCity() {
			return m_city;
		}

		public void setCity(String city) {
			m_city = city;
		}

		public String getChannel() {
			return m_channel;
		}

		public void setChannel(String channel) {
			m_channel = channel;
		}

	}
}
