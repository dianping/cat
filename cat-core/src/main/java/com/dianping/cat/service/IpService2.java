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
package com.dianping.cat.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.service.IpService.IpInfo;

@Named
public class IpService2 implements Initializable {

	private int m_offset;

	private int[] m_index = new int[65536];

	private ByteBuffer m_dataBuffer;

	private ByteBuffer m_indexBuffer;

	private ReentrantLock m_lock = new ReentrantLock();

	private long bytesToLong(byte a, byte b, byte c, byte d) {
		return int2long((((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff)));
	}

	private String[] find(String ip) {
		String[] ips = ip.split("\\.");
		int prefix_value = (Integer.valueOf(ips[0]) * 256 + Integer.valueOf(ips[1]));
		long ip2long_value = ip2long(ip);
		int start = m_index[prefix_value];
		int max_comp_len = m_offset - 262144 - 4;
		long tmpInt;
		long index_offset = -1;
		int index_length = -1;
		byte b = 0;

		for (start = start * 9 + 262144; start < max_comp_len; start += 9) {
			tmpInt = int2long(m_indexBuffer.getInt(start));
			if (tmpInt >= ip2long_value) {
				index_offset = bytesToLong(b, m_indexBuffer.get(start + 6), m_indexBuffer.get(start + 5),
										m_indexBuffer.get(start + 4));
				index_length = (0xFF & m_indexBuffer.get(start + 7) << 8) + (0xFF & m_indexBuffer.get(start + 8));
				break;
			}
		}

		byte[] areaBytes;

		m_lock.lock();

		try {
			m_dataBuffer.position(m_offset + (int) index_offset - 262144);
			areaBytes = new byte[index_length];
			m_dataBuffer.get(areaBytes, 0, index_length);
		} finally {
			m_lock.unlock();
		}

		return new String(areaBytes, Charset.forName("UTF-8")).split("\t", -1);
	}

	public IpInfo findIpInfoByString(String ip) {
		String[] infos = find(ip);

		if (infos.length >= 7) {
			IpInfo ipInfo = new IpInfo();

			ipInfo.setNation(infos[0]);
			ipInfo.setProvince(infos[1]);
			ipInfo.setCity(infos[2]);
			ipInfo.setChannel(infos[4]);
			ipInfo.setLatitude(infos[5]);
			ipInfo.setLongitude(infos[6]);

			return ipInfo;
		}

		return null;
	}

	@Override
	public void initialize() throws InitializationException {
		load("ip/ipdata.datx");
	}

	private long int2long(int i) {
		long l = i & 0x7fffffffL;
		if (i < 0) {
			l |= 0x080000000L;
		}
		return l;
	}

	private long ip2long(String ip) {
		return int2long(str2Ip(ip));
	}

	private void load(String filename) {
		m_lock.lock();

		try {
			InputStream is = IpService.class.getClassLoader().getResourceAsStream(filename);
			m_dataBuffer = ByteBuffer.wrap(Files.forIO().readFrom(is));
			m_dataBuffer.position(0);
			m_offset = m_dataBuffer.getInt(); // indexLength
			byte[] indexBytes = new byte[m_offset];
			m_dataBuffer.get(indexBytes, 0, m_offset - 4);
			m_indexBuffer = ByteBuffer.wrap(indexBytes);
			m_indexBuffer.order(ByteOrder.LITTLE_ENDIAN);

			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 256; j++) {
					m_index[i * 256 + j] = m_indexBuffer.getInt();
				}
			}
			m_indexBuffer.order(ByteOrder.BIG_ENDIAN);
		} catch (IOException e) {
			Cat.logError(e);
		} finally {
			m_lock.unlock();
		}
	}

	private int str2Ip(String ip) {
		String[] ss = ip.split("\\.");
		int a = Integer.parseInt(ss[0]);
		int b = Integer.parseInt(ss[1]);
		int c = Integer.parseInt(ss[2]);
		int d = Integer.parseInt(ss[3]);

		return (a << 24) | (b << 16) | (c << 8) | d;
	}

}
