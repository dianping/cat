package com.dianping.cat.consumer;

import org.unidal.lookup.logging.Logger;

public class MockLog implements Logger {

	@Override
	public void debug(String message) {

	}

	@Override
	public void debug(String message, Throwable throwable) {

	}

	@Override
	public boolean isDebugEnabled() {

		return false;
	}

	@Override
	public void info(String message) {

	}

	@Override
	public void info(String message, Throwable throwable) {

	}

	@Override
	public boolean isInfoEnabled() {

		return false;
	}

	@Override
	public void warn(String message) {

	}

	@Override
	public void warn(String message, Throwable throwable) {

	}

	@Override
	public boolean isWarnEnabled() {

		return false;
	}

	@Override
	public void error(String message) {

	}

	@Override
	public void error(String message, Throwable throwable) {

	}

	@Override
	public boolean isErrorEnabled() {

		return false;
	}

	@Override
	public int getThreshold() {

		return 0;
	}

	@Override
	public void setThreshold(int threshold) {

	}

	@Override
	public String getName() {

		return null;
	}

}