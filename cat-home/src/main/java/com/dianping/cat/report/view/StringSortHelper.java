package com.dianping.cat.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class StringSortHelper {
	public static List<String> sortDomain(List<String> lists) {
		Collections.sort(lists, new DomainComparator());
		return lists;
	}

	public static List<String> sortDomain(Set<String> lists) {
		if (lists == null) {
			return null;
		} else {
			List<String> domainsList = new ArrayList<String>();
			for (String domain : lists) {
				domainsList.add(domain);
			}
			return sortDomain(domainsList);
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
			if (d1.equals("Cat")) {
				return 1;
			}

			return d1.compareTo(d2);
		}
	}
}
