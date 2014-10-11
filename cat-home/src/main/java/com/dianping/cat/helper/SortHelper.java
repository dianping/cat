package com.dianping.cat.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.Constants;

public class SortHelper {

	private static DomainComparator s_domainComparator = new DomainComparator();

	private static IpComparator s_ipComparator = new IpComparator();

	public static List<String> sortDomain(Collection<String> strs) {
		if (strs == null) {
			return null;
		} else {
			List<String> result = new ArrayList<String>(strs);
			Collections.sort(result, s_domainComparator);

			return result;
		}
	}
	
	public static List<String> sortIpAddress(Collection<String> strs) {
		if (strs == null) {
			return null;
		} else {
			List<String> result = new ArrayList<String>(strs);
			Collections.sort(result, s_ipComparator);

			return result;
		}
	}

	public static <K, V> Map<K, V> sortMap(Map<K, V> map, Comparator<Entry<K, V>> compator) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(entries, compator);

		for (Entry<K, V> entry : entries) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static class DomainComparator implements Comparator<String> {
		@Override
		public int compare(String d1, String d2) {
			if (d1 == null && d2 == null) {
				return 0;
			} else if (d1 == null) {
				return 1;
			} else if (d2 == null) {
				return -1;
			}

			if (Constants.CAT.equals(d1)) {
				return 1;
			}
			if (Constants.CAT.equals(d2)) {
				return -1;
			}
			if (Constants.ALL.equals(d1)) {
				return -1;
			}
			if (Constants.ALL.equals(d2)) {
				return +1;
			}

			return d1.compareTo(d2);
		}
	}

	public static class IpComparator implements Comparator<String> {
		@Override
		public int compare(String d1, String d2) {
			if (d1 == null && d2 == null) {
				return 0;
			} else if (d1 == null) {
				return 1;
			} else if (d2 == null) {
				return -1;
			}
			if (Constants.ALL.equals(d1)) {
				return -1;
			}
			if (Constants.ALL.equals(d2)) {
				return +1;
			}

			return d1.compareTo(d2);
		}
	}
}
