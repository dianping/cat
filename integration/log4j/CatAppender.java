
package com.dianping.cat.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Trace;

public class CatAppender extends AppenderSkeleton {
	
	@Override
	protected void append(LoggingEvent event) {
		boolean isTraceMode = Cat.getManager().isTraceMode();
		Level level = event.getLevel();

		if (level.isGreaterOrEqual(Level.ERROR)) {
			logError(event);
		} else if (isTraceMode) {
			logTrace(event);
		}
	}

	private String buildExceptionStack(Throwable exception) {
		if (exception != null) {
			StringWriter writer = new StringWriter(2048);

			exception.printStackTrace(new PrintWriter(writer));
			return writer.toString();
		} else {
			return "";
		}
	}

	@Override
	public void close() {
	}

	private void logError(LoggingEvent event) {
		ThrowableInformation info = event.getThrowableInformation();

		if (info != null) {
			Throwable exception = info.getThrowable();
			Object message = event.getMessage();

			if (message != null) {
				Cat.logError(String.valueOf(message), exception);
			} else {
				Cat.logError(exception);
			}
		}
	}

	private void logTrace(LoggingEvent event) {
		String type = "Log4j";
		String name = event.getLevel().toString();
		Object message = event.getMessage();
		String data;

		if (message instanceof Throwable) {
			data = buildExceptionStack((Throwable) message);
		} else {
			data = event.getMessage().toString();
		}

		ThrowableInformation info = event.getThrowableInformation();

		if (info != null) {
			data = data + '\n' + buildExceptionStack(info.getThrowable());
		}
		Cat.logTrace(type, name, Trace.SUCCESS, data);
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
}
