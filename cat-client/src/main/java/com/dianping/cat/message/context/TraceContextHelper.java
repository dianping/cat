package com.dianping.cat.message.context;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.message.pipeline.MessagePipeline;

public class TraceContextHelper {
	private static final String CAT_MESSAGE_CONTEXT = "CAT_MESSAGE_CONTEXT";

	private static ThreadLocal<TraceContext> s_threadLocalContext = new ThreadLocal<TraceContext>();

	private static AtomicBoolean s_initialized = new AtomicBoolean();

	private static MessagePipeline s_pipeline;

	private static MessageIdFactory s_factory;

	public static String createMessageId() {
		initialize();

		return s_factory.getNextId();
	}

	public static String createMessageId(String domain) {
		initialize();

		if (domain == null) {
			return s_factory.getNextId();
		} else {
			return s_factory.getNextId(domain);
		}
	}

	public static TraceContext extractFrom(HttpServletRequest req) {
		Object ctx = req.getAttribute(CAT_MESSAGE_CONTEXT);

		if (ctx instanceof TraceContext) {
			return (TraceContext) ctx;
		}

		throw new RuntimeException("No MessageContext found in " + req);
	}

	private static void initialize() {
		if (!s_initialized.get()) {
			Cat.getBootstrap().initialize(new ClientConfig());

			ComponentContext context = Cat.getBootstrap().getComponentContext();

			s_pipeline = context.lookup(MessagePipeline.class);
			s_factory = context.lookup(MessageIdFactory.class);
			s_initialized.set(true);
		}
	}

	public static void injectTo(HttpServletRequest req) {
		TraceContext ctx = threadLocal();

		req.setAttribute(CAT_MESSAGE_CONTEXT, ctx);
	}

	public static void reset() {
		s_threadLocalContext.remove();
		s_initialized.set(false);
		s_pipeline = null;
		s_factory = null;
	}

	public static TraceContext threadLocal() {
		initialize();

		TraceContext ctx = s_threadLocalContext.get();

		if (ctx == null) {
			ctx = new DefaultTraceContext(s_pipeline, s_factory);

			s_threadLocalContext.set(ctx);
		}

		return ctx;
	}
}
