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
package com.dianping.cat.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AggregationMessageFormat {

	private List<String> m_formatTokens = new ArrayList<String>();

	private MessageFormat m_messageFormat;

	public AggregationMessageFormat(String pattern) {
		m_messageFormat = new MessageFormat(build(pattern));
	}

	private String build(String pattern) {
		int index = 0;
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher matcher = p.matcher(pattern);
		StringBuffer output = new StringBuffer();

		while (matcher.find()) {
			m_formatTokens.add(matcher.group(1).trim());
			matcher.appendReplacement(output, "{" + index + "}");
			if (index < 9) {
				index++;
			}
		}
		matcher.appendTail(output);
		return output.toString();
	}

	public List<String> getFormatTokens() {
		return m_formatTokens;
	}

	public MessageFormat getMessageFormat() {
		return (MessageFormat) m_messageFormat.clone();
	}

}
