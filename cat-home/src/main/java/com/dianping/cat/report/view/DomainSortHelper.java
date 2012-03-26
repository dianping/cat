package com.dianping.cat.report.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DomainSortHelper {
	public static List<String> sortDomain(List<String> domains){
		Collections.sort(domains,new DomainComparator());
		return domains;
	}
	
	public static List<String> sortDomain(Set<String> domains){
		if(domains==null){
			return null;
		}else{
			List<String> domainsList = new ArrayList<String>();
			for(String domain:domains){
				domainsList.add(domain);
			}
			return sortDomain(domainsList);
		}
	}
	
	public static class DomainComparator implements Comparator<String>{
		@Override
		public int compare(String d1, String d2) {
			if (d1.equals("Cat")) {
				return 1;
			}

			return d1.compareTo(d2);
		}
	}
}
