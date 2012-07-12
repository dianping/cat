package com.dianping.cat.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class StringSortHelper {
	public static List<String> sort(List<String> lists) {
		Collections.sort(lists);
		return lists;
	}

	public static List<String> sort(Set<String> lists) {
		if (lists == null) {
			return null;
		} else {
			List<String> result = new ArrayList<String>();
			for (String temp : lists) {
				result.add(temp);
			}
			return sort(result);
		}
	}

	public static List<String> sortDomain(List<String> lists) {
		Collections.sort(lists, new DomainComparator());
		return lists;
	}

	public static List<String> sortDomain(Set<String> lists) {
		if (lists == null) {
			return null;
		} else {
			List<String> result = new ArrayList<String>();
			for (String domain : lists) {
				result.add(domain);
			}
			return sortDomain(result);
		}
	}

	public static List<String> sortString(Set<String> lists) {
		if (lists == null) {
			return null;
		} else {
			List<String> result = new ArrayList<String>();
			for (String domain : lists) {
				result.add(domain);
			}
			Collections.sort(result);
			return result;
		}
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

			if ("Cat".equals(d1)) {
				return 1;
			}
			if ("Cat".equals(d2)) {
				return -1;
			}
			if ("All".equals(d1)) {
				return -1;
			}
			if ("All".equals(d2)) {
				return +1;
			}

			return d1.compareTo(d2);
		}
	}
}
