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
package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class AtomicTreeParser {

	private List<String> m_startTypes = new ArrayList<String>();

	private List<String> m_matchTypes = new ArrayList<String>();

	public void init(String startTypes, String matchTypes) {
		if (startTypes != null) {
			m_startTypes = Splitters.by(";").noEmptyItem().split(startTypes);
		}
		if (matchTypes != null) {
			m_matchTypes = Splitters.by(";").noEmptyItem().split(matchTypes);
		}
	}

	public boolean isAtomicMessage(MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String type = message.getType();

			if (m_startTypes != null) {
				for (String s : m_startTypes) {
					if (type.startsWith(s)) {
						return true;
					}
				}
			}
			if (m_matchTypes != null) {
				return m_matchTypes.contains(type);
			}
			return false;
		} else {
			return true;
		}
	}

}
