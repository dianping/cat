package com.dianping.cat.notify.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class MapUtils {
	public static <K, V> Map<K, V> sortMap(Map<K, V> map, Comparator<Entry<K, V>> compator) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(entries,compator);
		for(Entry<K,V> entry:entries){
			result.put(entry.getKey(), entry.getValue());
		}
		
		return result;
	}
	
//	public <T,S> List<String> sort(T model, String xpath, Comparator<S> comparator) {
//		TransactionReport report = null;
//		List<String> keys = sort(report, "/transaction-report/type[@name='URL']/name", new Comparator<TransactionName>() {
//			@Override
//         public int compare(TransactionName n1, TransactionName n2) {
//	         return 0;
//         }
//		});
//		
//		return null;
//	}
}
