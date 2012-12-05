package com.dianping.cat.job.spi.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class MapWritable implements WritableComparable<MapWritable> {
	private Map<String, String> m_map = new LinkedHashMap<String, String>();

	@Override
	public int compareTo(MapWritable o) {
		for (Map.Entry<String, String> e : m_map.entrySet()) {
			String key = e.getKey();
			String v1 = e.getValue();
			String v2 = o.m_map.get(key);

			if (v1 == null && v2 == null) {
				continue;
			} else if (v1 == null && v2 != null) {
				return -1;
			} else if (v1 != null && v2 == null) {
				return 1;
			} else {
				int result = v1.compareTo(v2);

				if (result != 0) {
					return result;
				}
			}
		}

		return 0;
	}

	public String get(String key) {
		return m_map.get(key);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Object obj = get(key);

		try {
			if (obj != null) {
				return Boolean.parseBoolean(obj.toString());
			}
		} catch (NumberFormatException e) {
			// ignore it
		}

		return defaultValue;
	}

	public double getDouble(String key, double defaultValue) {
		Object obj = get(key);

		try {
			if (obj != null) {
				return Double.parseDouble(obj.toString());
			}
		} catch (NumberFormatException e) {
			// ignore it
		}

		return defaultValue;
	}

	public float getFloat(String key, float defaultValue) {
		Object obj = get(key);

		try {
			if (obj != null) {
				return Float.parseFloat(obj.toString());
			}
		} catch (NumberFormatException e) {
			// ignore it
		}

		return defaultValue;
	}

	public int getInt(String key, int defaultValue) {
		Object obj = get(key);

		try {
			if (obj != null) {
				return Integer.parseInt(obj.toString());
			}
		} catch (NumberFormatException e) {
			// ignore it
		}

		return defaultValue;
	}

	public long getLong(String key, long defaultValue) {
		Object obj = get(key);

		try {
			if (obj != null) {
				return Long.parseLong(obj.toString());
			}
		} catch (NumberFormatException e) {
			// ignore it
		}

		return defaultValue;
	}

	public boolean has(String key) {
		return m_map.containsKey(key);
	}

	public MapWritable put(String key, Object value) {
		m_map.put(key, value == null ? null : value.toString());
		return this;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		String str = Text.readString(in);
		KeyValueInput input = new KeyValueInput(str);

		while (input.next()) {
			String key = input.getKey();
			String value = input.getValue();

			m_map.put(key, value);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		KeyValueOutput output = new KeyValueOutput();

		for (Map.Entry<String, String> e : m_map.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();

			output.add(key, value);
		}

		Text.writeString(out, output.toString());
	}

	static class KeyValueInput {
		private String m_source;

		private int m_index;

		private StringBuilder m_key;

		private StringBuilder m_value;

		private boolean m_flag;

		public KeyValueInput(String source) {
			m_source = source;
			m_key = new StringBuilder();
			m_value = new StringBuilder();
		}

		public String getKey() {
			return m_key.toString();
		}

		public String getValue() {
			return m_flag ? m_value.toString() : null;
		}

		public boolean next() {
			int len = m_source.length();

			m_flag = false;
			m_key.setLength(0);
			m_value.setLength(0);

			if (m_index >= len) {
				return false;
			}

			while (m_index < len) {
				char ch = m_source.charAt(m_index++);

				switch (ch) {
				case '=':
					if (!m_flag) {
						m_flag = true;
					} else {
						m_value.append(ch);
					}
					break;
				case '&':
					return true;
				case '\\':
					if (m_index + 1 < len) {
						char ch2 = m_source.charAt(m_index++);

						if (ch2 == 'r') {
							ch = '\r';
						} else if (ch2 == 'n') {
							ch = '\n';
						} else {
							ch = ch2;
						}
					}
					// break through
				default:
					if (!m_flag) {
						m_key.append(ch);
					} else {
						m_value.append(ch);
					}
				}
			}

			return true;
		}
	}

	static class KeyValueOutput {
		StringBuilder m_sb = new StringBuilder(256);

		void add(String str) {
			int len = str.length();

			for (int i = 0; i < len; i++) {
				char ch = str.charAt(i);

				switch (ch) {
				case '\r':
					m_sb.append("\\r");
					break;
				case '\n':
					m_sb.append("\\n");
					break;
				case '\\':
				case '=':
				case '&':
					m_sb.append('\\');
					m_sb.append(ch);
					break;
				default:
					m_sb.append(ch);
					break;
				}
			}
		}

		public void add(String key, String value) {
			if (m_sb.length() > 0) {
				m_sb.append('&');
			}

			add(key);

			if (value != null) {
				m_sb.append('=');
				add(value);
			}
		}

		@Override
		public String toString() {
			return m_sb.toString();
		}
	}
}
