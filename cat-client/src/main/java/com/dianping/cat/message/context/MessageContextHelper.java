package com.dianping.cat.message.context;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.message.pipeline.MessagePipeline;

public class MessageContextHelper {
	private static final String CAT_MESSAGE_CONTEXT = "CAT_MESSAGE_CONTEXT";

	private static ThreadLocal<MessageContext> s_threadLocalContext = new ThreadLocal<MessageContext>();

	private static AtomicBoolean s_initialized = new AtomicBoolean();

	private static MessagePipeline s_pipeline;

	private static MessageIdFactory s_factory;

	public static MessageContext extractFrom(HttpServletRequest req) {
		Object ctx = req.getAttribute(CAT_MESSAGE_CONTEXT);

		if (ctx instanceof MessageContext) {
			return (MessageContext) ctx;
		}

		throw new RuntimeException("No MessageContext found in " + req);
	}

	public static void injectTo(HttpServletRequest req) {
		MessageContext ctx = threadLocal();

		req.setAttribute(CAT_MESSAGE_CONTEXT, ctx);
	}

	public static void reset() {
		s_threadLocalContext.remove();
		s_initialized.set(false);
		s_pipeline = null;
		s_factory = null;
	}

	public static MessageContext threadLocal() {
		if (!s_initialized.get()) {
			Cat.getBootstrap().initialize(new ClientConfig());

			ComponentContext context = Cat.getBootstrap().getComponentContext();

			s_pipeline = context.lookup(MessagePipeline.class);
			s_factory = context.lookup(MessageIdFactory.class);
			s_initialized.set(true);
		}

		MessageContext ctx = s_threadLocalContext.get();

		if (ctx == null) {
			ctx = new DefaultMessageContext(s_pipeline, s_factory);

			s_threadLocalContext.set(ctx);
		}

		return ctx;
	}
}
