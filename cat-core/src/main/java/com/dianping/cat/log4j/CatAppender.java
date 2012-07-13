package com.dianping.cat.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

public class CatAppender extends AppenderSkeleton {
	@Override
	protected void append(LoggingEvent event) {
		if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
			ThrowableInformation throwableInformation = event.getThrowableInformation();

			if (throwableInformation != null) {
				MessageProducer cat = Cat.getProducer();
				Throwable exception = throwableInformation.getThrowable();
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
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
}
