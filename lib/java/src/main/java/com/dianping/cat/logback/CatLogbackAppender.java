package com.dianping.cat.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;
import com.dianping.cat.Cat;

public class CatLogbackAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        try {
            Level level = event.getLevel();

            if (level.isGreaterOrEqual(Level.ERROR)) {
                logError(event);
            }
        } catch (Exception ex) {
            throw new LogbackException(event.getFormattedMessage(), ex);
        }
    }

    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();

        if (info != null) {
            Throwable exception = info.getThrowable();

            Object message = event.getFormattedMessage();
            if (message != null) {
                Cat.logError(String.valueOf(message), exception);
            } else {
                Cat.logError(exception);
            }
        }
    }

}
