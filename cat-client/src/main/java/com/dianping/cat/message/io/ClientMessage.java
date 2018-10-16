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
package com.dianping.cat.message.io;

import java.text.ParseException;
import java.util.Iterator;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;
import com.dianping.cat.util.json.JsonObject;

public class ClientMessage {

	/**
		* "catc" -> 0x63617463 -> 1667331171
		*/
	public final static int PROTOCOL_ID = 1667331171;

	public final static int VERSION_0 = 0;

	private final int m_protocolId;

	private final int m_version;

	private final byte[] m_data;

	public ClientMessage(int protocolId, int version, byte[] data) {
		m_protocolId = protocolId;
		m_version = version;
		m_data = data;
	}

	public ClientMessage(int protocolId, int version, String data) {
		m_protocolId = protocolId;
		m_version = version;
		m_data = data.getBytes();
	}

	public static void main(String[] args) {
		byte[] bytes = "catc".getBytes();
		int ret = 0;
		for (int i = 0; i < 4; i++) {
			ret <<= 8;
			ret |= bytes[i] & 0xFF;
		}
		System.out.println(ret);
	}

	public byte[] getData() {
		return m_data;
	}

	public int getProtocolId() {
		return m_protocolId;
	}

	public int getVersion() {
		return m_version;
	}

	public PropertyConfig toPropertyConfig() {
		JsonObject object;
		try {
			object = new JsonObject(new String(m_data));
		} catch (ParseException e) {
			Cat.logError(e);
			return null;
		}

		JsonObject kvs = object.getJSONObject("kvs");
		if (kvs == null) {
			return null;
		}

		PropertyConfig config = new PropertyConfig();
		for (Iterator<String> it = kvs.keys(); it.hasNext(); ) {
			String key = it.next();
			String val = kvs.getString(key);
			if (val != null) {
				config.addProperty(new Property().setId(key).setValue(val));
			}
		}
		return config;
	}
}
