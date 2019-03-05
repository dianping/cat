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
package com.dianping.cat.message.internal;

import java.util.concurrent.locks.LockSupport;

/**
	* This timer provides milli-second precise system time.
	*/
public class MilliSecondTimer {
	private static long m_baseTime;

	private static long m_startNanoTime;

	private static boolean m_isWindows = false;

	public static long currentTimeMillis() {
		if (m_isWindows) {
			if (m_baseTime == 0) {
				initialize();
			}

			long elipsed = (long) ((System.nanoTime() - m_startNanoTime) / 1e6);

			return m_baseTime + elipsed;
		} else {
			return System.currentTimeMillis();
		}
	}

	public static void initialize() {
		String os = System.getProperty("os.name");

		if (os.startsWith("Windows")) {
			m_isWindows = true;
			m_baseTime = System.currentTimeMillis();

			while (true) {
				LockSupport.parkNanos(100000); // 0.1 ms

				long millis = System.currentTimeMillis();

				if (millis != m_baseTime) {
					m_baseTime = millis;
					m_startNanoTime = System.nanoTime();
					break;
				}
			}
		} else {
			m_baseTime = System.currentTimeMillis();
			m_startNanoTime = System.nanoTime();
		}
	}
}
