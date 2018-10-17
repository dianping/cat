/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
