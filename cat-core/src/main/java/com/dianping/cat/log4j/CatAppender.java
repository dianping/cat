package com.dianping.cat.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.dianping.cat.Cat;

public class CatAppender extends AppenderSkeleton {
	@Override
	protected void append(LoggingEvent event) {
		if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
			ThrowableInformation throwableInformation = event.getThrowableInformation();

			if (throwableInformation != null) {
				if (Cat.getManager().getThreadLocalMessageTree() != null) {
					Cat.getProducer().logError(throwableInformation.getThrowable());
//				} else {
//					Cat.setup(null);
//					Cat.getProducer().logError(throwableInformation.getThrowable());
//					Cat.reset();
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
