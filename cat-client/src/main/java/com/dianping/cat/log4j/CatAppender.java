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
		boolean isTraceMode = Cat.getManager().hasContext();
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

	private void logTrace(LoggingEvent event) {
		String type = "Trace";
		String name = event.getLevel().toString();
		String data = event.getMessage().toString();
		ThrowableInformation info = event.getThrowableInformation();

		if (info != null) {
			Throwable exception = info.getThrowable();

			if (exception != null) {
				StringWriter writer = new StringWriter(2048);

				exception.printStackTrace(new PrintWriter(writer));
				data = data + '\n' + writer.toString();
			}
		}
		Cat.logTrace(type, name, Trace.SUCCESS, data);
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
}
