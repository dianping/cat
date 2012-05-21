package com.dianping.cat.report.page.heatmap;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

	private List<Location> m_locations = new ArrayList<Location>();

	private int m_max = Integer.MIN_VALUE;

	private int m_min = Integer.MAX_VALUE;

	public String getJsonString() {
		String data = getLocationsString();
		int min = m_min;
		int max = m_max;
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("min:").append(min).append(",");
		sb.append("max:").append(max).append(",");
		sb.append("data:").append(data);
		sb.append("}");
		return sb.toString();
	}

	public String getLocationsString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int size = m_locations.size();
		for (int i = 0; i < size; i++) {
			sb.append(getJsonByLocation(m_locations.get(i)));
			if (i < size - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private String getJsonByLocation(Location location) {
		StringBuilder sb = new StringBuilder();
		int count = location.getCount();
		sb.append("[").append(location.getLat()).append(",");
		sb.append(location.getLng()).append(",");
		sb.append(count).append("]");
		m_max = m_max < count ? count : m_max;
		m_min = m_min > count ? count : m_min;

		return sb.toString();
	}

	public LocationData addLocationInfo(Location location) {
		m_locations.add(location);
		return this;
	}

	public List<Location> getLocations() {
		return m_locations;
	}

	public void setLocations(List<Location> locations) {
		m_locations = locations;
	}

	public static class Location {
		private double m_lat;

		private double m_lng;

		private int m_count;

		public Location(double lat, double lng, int count) {
			m_lat = lat;
			m_lng = lng;
			m_count = count;
		}

		public double getLat() {
			return m_lat;
		}

		public void setLat(double lat) {
			m_lat = lat;
		}

		public double getLng() {
			return m_lng;
		}

		public void setLng(double lng) {
			m_lng = lng;
		}

		public int getCount() {
			return m_count;
		}

		public void setCount(int count) {
			m_count = count;
		}
	}
}
