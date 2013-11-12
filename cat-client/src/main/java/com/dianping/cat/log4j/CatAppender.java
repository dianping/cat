package com.dianping.cat.log4j;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

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

	@Override
	public void close() {
	}

	private void logError(LoggingEvent event) {
		ThrowableInformation info = event.getThrowableInformation();

		if (info != null) {
			MessageProducer cat = Cat.getProducer();
			Throwable exception = info.getThrowable();
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

			if (tree == null) {
				Transaction t = cat.newTransaction("System", "Log4jException");

				cat.logError(exception);
				t.setStatus(Message.SUCCESS);
				t.complete();
			} else {
				cat.logError(exception);
			}
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
