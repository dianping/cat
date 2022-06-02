package com.dianping.cat.component;

public class LoggerWrapper implements Logger {
	private Logger m_logger;

	public LoggerWrapper(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void debug(String format, Object... args) {
		m_logger.debug(format, args);
	}

	@Override
	public void debug(Throwable cause, String format, Object... args) {
		m_logger.debug(cause, format, args);
	}

	@Override
	public void error(String format, Object... args) {
		m_logger.error(format, args);
	}

	@Override
	public void error(Throwable cause, String format, Object... args) {
		m_logger.error(cause, format, args);
	}

	@Override
	public void info(String format, Object... args) {
		m_logger.info(format, args);
	}

	@Override
	public void info(Throwable cause, String format, Object... args) {
		m_logger.info(cause, format, args);
	}

	public void setLogger(Logger logger) {
		m_logger = logger;
		
	}

	@Override
	public void warn(String format, Object... args) {
		m_logger.warn(format, args);
	}

	@Override
	public void warn(Throwable cause, String format, Object... args) {
		m_logger.warn(cause, format, args);
	}
}