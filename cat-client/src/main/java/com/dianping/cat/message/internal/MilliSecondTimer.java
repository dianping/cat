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
