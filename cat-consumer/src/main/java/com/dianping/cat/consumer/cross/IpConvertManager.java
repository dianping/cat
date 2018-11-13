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
package com.dianping.cat.consumer.cross;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.dianping.cat.Cat;

public class IpConvertManager {

	private Map<String, String> m_hosts = new HashMap<String, String>();

	public String convertHostNameToIP(String hostName) {
		String result = m_hosts.get(hostName);

		if (result == null) {
			if (isIPAddress(hostName)) {
				result = hostName;
			} else {
				try {
					InetAddress address = InetAddress.getByName(hostName);

					result = address.getHostAddress();
				} catch (Exception e) {
					Cat.logError(e);
					result = "";
				}
			}
			m_hosts.put(hostName, result);
		}
		return result;
	}

	public boolean isIPAddress(String str) {
		Pattern pattern = Pattern
								.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

}
