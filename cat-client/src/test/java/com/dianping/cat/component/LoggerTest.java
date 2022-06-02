package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.DefaultLogger.Output;
import com.dianping.cat.component.Logger.Level;

public class LoggerTest {
	@Test
	public void testAPI() {
		ComponentContext ctx = new DefaultComponentContext();
		Logger logger = ctx.lookup(Logger.class);

		logger.debug("Hello, %s", "world");
		logger.info("Hello, %s", "world");
		logger.warn("Hello, %s", "world");
		logger.error("Hello, %s", "world");
	}

	@Test
	public void testUserDefinedLogger() {
		ComponentContext ctx = new DefaultComponentContext();
		final StringBuilder sb = new StringBuilder();

		Logger oldLogger = ctx.lookup(Logger.class);

		oldLogger.info("message");
		Assert.assertEquals("", sb.toString());

		ctx.registerComponent(Logger.class, new DefaultLogger().output(new Output() {
			@Override
			public void write(Level level, String message, Throwable cause) {
				sb.append(level).append(": ").append(message);
			}
		}));

		// new logger will use the new Logger
		Logger newLogger = ctx.lookup(Logger.class);

		newLogger.info("message");
		Assert.assertEquals("INFO: message", sb.toString());

		// old logger should use the new Logger
		sb.setLength(0);

		oldLogger.info("message");
		Assert.assertEquals("INFO: message", sb.toString());
	}
}
