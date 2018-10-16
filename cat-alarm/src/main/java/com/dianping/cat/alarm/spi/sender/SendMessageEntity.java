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
package com.dianping.cat.alarm.spi.sender;

import java.util.List;

public class SendMessageEntity {
	private String m_group;

	private String m_title;

	private String m_type;

	private String m_content;

	private List<String> m_receivers;

	public SendMessageEntity(String group, String title, String type, String content, List<String> receivers) {
		m_group = group;
		m_title = title;
		m_type = type;
		m_content = content;
		m_receivers = receivers;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public String getGroup() {
		return m_group;
	}

	public List<String> getReceivers() {
		return m_receivers;
	}

	public String getReceiverString() {
		StringBuilder builder = new StringBuilder(100);

		for (String receiver : m_receivers) {
			builder.append(receiver).append(",");
		}

		String tmpResult = builder.toString();
		if (tmpResult.endsWith(",")) {
			return tmpResult.substring(0, tmpResult.length() - 1);
		} else {
			return tmpResult;
		}
	}

	public String getTitle() {
		return m_title;
	}

	public String getType() {
		return m_type;
	}

	@Override
	public String toString() {
		return "SendMessageEntity [group=" + m_group + ", title=" + m_title + ", type=" + m_type + ", content="	+ m_content
								+ ", receivers=" + m_receivers + "]";
	}

}