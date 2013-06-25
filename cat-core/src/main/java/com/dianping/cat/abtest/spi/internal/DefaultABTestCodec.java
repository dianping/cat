package com.dianping.cat.abtest.spi.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DefaultABTestCodec implements ABTestCodec {

	private Map<String, Map<String, String>> m_codes = new LinkedHashMap<String, Map<String, String>>(1000, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Map<String, String>> arg0) {
			return true;
		}
	};

	public Map<String, Map<String, String>> decode(String value, Set<String> keys) {
		int len = value.length();
		Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
		StringBuilder key = new StringBuilder();
		StringBuilder name = new StringBuilder();
		StringBuilder val = new StringBuilder();
		int part = 0;

		for (int i = 0; i < len; i++) {
			char ch = value.charAt(i);

			switch (ch) {
			case '&':
				newCookielet(map, keys, key, name, val);

				key.setLength(0);
				name.setLength(0);
				val.setLength(0);
				part = 0;
				break;
			case '=':
				if (part == 0) {
					part = 1;
				} else {
					distribute(key, name, val, part, ch);
				}
				break;
			case '|':
				if (part == 2) {
					newCookielet(map, keys, key, name, val);

					name.setLength(0);
					val.setLength(0);

					part = 1;
				} else {
					distribute(key, name, val, part, ch);
				}
				break;
			case ':':
				if (part == 1) {
					part = 2;
				} else {
					distribute(key, name, val, part, ch);
				}
				break;
			default:
				distribute(key, name, val, part, ch);

				break;
			}
		}

		newCookielet(map, keys, key, name, val);

		return map;
	}

	private void distribute(StringBuilder key, StringBuilder name, StringBuilder val, int part, char ch) {
		if (part == 0) {
			key.append(ch);
		} else if (part == 1) {
			name.append(ch);
		} else if (part == 2) {
			val.append(ch);
		}
	}

	private void newCookielet(Map<String, Map<String, String>> map, Set<String> keys, StringBuilder key,
	      StringBuilder name, StringBuilder value) {
		String k = key.toString();

		if (keys == null || keys.contains(k)) {
			String n = name.toString();
			String v = value.toString();

			if (k.length() > 0 && n.length() > 0) {
				Map<String, String> cookielets = map.get(k);

				if (cookielets == null) {
					cookielets = new LinkedHashMap<String, String>();
					map.put(k, cookielets);
				}

				cookielets.put(n, v);
			}
		}
	}

	@Override
	public String encode(Map<String, Map<String, String>> map) {
		StringBuilder sb = new StringBuilder(32);
		boolean first = true;

		for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
			String runId = entry.getKey();
			Map<String, String> cookielets = entry.getValue();

			if (first) {
				first = false;
			} else {
				sb.append('&');
			}

			boolean first2 = true;

			sb.append(runId).append('=');

			for (Map.Entry<String, String> e : cookielets.entrySet()) {
				if (first2) {
					first2 = false;
				} else {
					sb.append('|');
				}

				sb.append(e.getKey()).append(':').append(e.getValue());
			}
		}

		return sb.toString();
	}

	public Map<String, String> decode(String value) {
		Map<String, String> code = m_codes.get(value);

		if (code == null) {
			Map<String, Map<String, String>> maps = decode(value, null);

			code = new LinkedHashMap<String, String>();

			for (Entry<String, Map<String, String>> entry : maps.entrySet()) {
				String key = entry.getKey();
				if (entry.getValue() != null) {
					String val = entry.getValue().get("ab");

					if (val != null) {
						code.put(key, val);
					}
				}
			}
			m_codes.put(value, code);
		}
		return code;
	}
}
