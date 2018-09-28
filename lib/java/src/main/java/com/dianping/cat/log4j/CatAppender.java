package com.dianping.cat.log4j;

import com.dianping.cat.Cat;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class CatAppender extends AppenderSkeleton {

    @Override
    protected void append(LoggingEvent event) {
        Level level = event.getLevel();

        if (level.isGreaterOrEqual(Level.ERROR)) {
            logError(event);
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

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
