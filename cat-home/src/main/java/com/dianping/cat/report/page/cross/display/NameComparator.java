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
package com.dianping.cat.report.page.cross.display;

import java.util.Comparator;

public class NameComparator implements Comparator<NameDetailInfo> {

	private String m_sorted;

	public NameComparator(String sort) {
		m_sorted = sort;
	}

	@Override
	public int compare(NameDetailInfo m1, NameDetailInfo m2) {
		if (m1.getId() != null && m1.getId().startsWith("All")) {
			return -1;
		}
		if (m2.getId() != null && m2.getId().startsWith("All")) {
			return 1;
		}

		if (m_sorted.equals("name")) {
			return m1.getId().compareTo(m2.getId());
		}
		if (m_sorted.equals("total")) {
			return (int) (m2.getTotalCount() - m1.getTotalCount());
		}
		if (m_sorted.equals("failure")) {
			return (int) (m2.getFailureCount() - m1.getFailureCount());
		}
		if (m_sorted.equals("failurePercent")) {
			return (int) (m2.getFailurePercent() * 1000 - m1.getFailurePercent() * 1000);
		}
		if (m_sorted.equals("avg")) {
			return (int) (m2.getAvg() * 1000 - m1.getAvg() * 1000);
		}
		return 0;
	}
}