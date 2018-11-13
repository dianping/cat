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
package com.dianping.cat.report;

import java.util.ArrayList;
import java.util.List;

public class LogMsg implements Comparable<LogMsg> {

	private final int MAX_ID_COUNT = 60;

	private String m_msg;

	private int m_count;

	private List<Integer> m_ids = new ArrayList<Integer>();

	public void addCount() {
		m_count++;
	}

	public void addId(int id) {
		if (m_ids.size() < MAX_ID_COUNT) {
			m_ids.add(id);
		}
	}

	@Override
	public int compareTo(LogMsg o) {
		return o.getCount() - m_count;
	}

	public int getCount() {
		return m_count;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public List<Integer> getIds() {
		return m_ids;
	}

	public void setIds(List<Integer> ids) {
		m_ids = ids;
	}

	public String getMsg() {
		return m_msg;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

}
