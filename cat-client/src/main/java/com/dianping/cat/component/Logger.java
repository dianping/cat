package com.dianping.cat.component;

import com.dianping.cat.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface Logger {
	public void debug(String format, Object... args);

	public void debug(Throwable cause, String format, Object... args);

	public void error(String format, Object... args);

	public void error(Throwable cause, String format, Object... args);

	public void info(String format, Object... args);

	public void info(Throwable cause, String format, Object... args);

	public void warn(String format, Object... args);

	public void warn(Throwable cause, String format, Object... args);

	public enum Level {
		DEBUG(0),

		INFO(1),

		WARN(2),

		ERROR(3);

		private int m_level;

		private Level(int level) {
			m_level = level;
		}

		public int getLevel() {
			return m_level;
		}

		public boolean isDebugEnabled() {
			return DEBUG.getLevel() >= m_level;
		}

		public boolean isErrorEnabled() {
			return ERROR.getLevel() >= m_level;
		}

		public boolean isInfoEnabled() {
			return INFO.getLevel() >= m_level;
		}

		public boolean isWarnEnabled() {
			return WARN.getLevel() >= m_level;
		}
	}

	public static class NoopLogger implements Logger {
		@Override
		public void debug(String format, Object... args) {
		}

		@Override
		public void debug(Throwable cause, String format, Object... args) {
		}

		@Override
		public void error(String format, Object... args) {
		}

		@Override
		public void error(Throwable cause, String format, Object... args) {
		}

		@Override
		public void info(String format, Object... args) {
		}

		@Override
		public void info(Throwable cause, String format, Object... args) {
		}

		@Override
		public void warn(String format, Object... args) {
		}

		@Override
		public void warn(Throwable cause, String format, Object... args) {
		}
	}
}
