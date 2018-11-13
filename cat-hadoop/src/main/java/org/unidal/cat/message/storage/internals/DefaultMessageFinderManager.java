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
package org.unidal.cat.message.storage.internals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import org.unidal.cat.message.storage.MessageFinder;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.internal.MessageId;

@Named(type = MessageFinderManager.class)
public class DefaultMessageFinderManager implements MessageFinderManager {

	private Map<Integer, List<MessageFinder>> m_map = new HashMap<Integer, List<MessageFinder>>();

	@Override
	public synchronized void close(int hour) {
		m_map.remove(hour);
	}

	@Override
	public ByteBuf find(MessageId id) {
		int hour = id.getHour();
		List<MessageFinder> finders = m_map.get(hour);

		if (finders != null) {
			for (MessageFinder finder : finders) {
				ByteBuf buf = finder.find(id);

				if (buf != null) {
					return buf;
				}
			}
		}

		return null;
	}

	@Override
	public void register(int hour, MessageFinder finder) {
		List<MessageFinder> finders = m_map.get(hour);

		if (finders == null) {
			synchronized (m_map) {
				finders = m_map.get(hour);

				if (finders == null) {
					finders = new ArrayList<MessageFinder>();

					m_map.put(hour, finders);
				}
			}
		}

		finders.add(finder);
	}

}
