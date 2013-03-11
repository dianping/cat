package com.dianping.cat.message.internal;

import java.util.concurrent.locks.LockSupport;

/**
 * This timer provides milli-second precise system time.
 */
public class MilliSecondTimer {
	private static long m_baseTime;

	private static long m_startNanoTime;
	
	public static void initialize() {
		String os = System.getProperty("os.name");

		if (os.startsWith("Windows")) {
			m_baseTime = System.currentTimeMillis();

			while (true) {
				LockSupport.parkNanos(1000000); // 1 ms

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

	public static long currentTimeMicros() {
		if (m_baseTime == 0) {
			initialize();
		}

		long elipsed = (long) ((System.nanoTime() - m_startNanoTime) / 1e3);

		return m_baseTime * 1000L + elipsed;
	}

	public static long currentTimeMillis() {
		if (m_baseTime == 0) {
			initialize();
		}

		long elipsed = (long) ((System.nanoTime() - m_startNanoTime) / 1e6);

		return m_baseTime + elipsed;
	}
}
