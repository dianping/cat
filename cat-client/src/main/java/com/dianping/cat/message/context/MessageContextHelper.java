package com.dianping.cat.message.context;

import javax.servlet.http.HttpServletRequest;

public class MessageContextHelper {
	private static final String CAT_MESSAGE_CONTEXT = "CAT_MESSAGE_CONTEXT";

	private static ThreadLocal<MessageContext> s_threadLocalContext = new ThreadLocal<MessageContext>();

	public static MessageContext extractFrom(HttpServletRequest req) {
		Object ctx = req.getAttribute(CAT_MESSAGE_CONTEXT);

		if (ctx instanceof MessageContext) {
			return (MessageContext) ctx;
		}

		throw new RuntimeException("No MessageContext found in " + req);
	}

	public static MessageContext getThreadLocal() {
		MessageContext ctx = s_threadLocalContext.get();

		if (ctx == null) {
			ctx = new DefaultMessageContext();

			s_threadLocalContext.set(ctx);
		}

		return ctx;
	}

	public static void injectTo(HttpServletRequest req) {
		MessageContext ctx = getThreadLocal();

		req.setAttribute(CAT_MESSAGE_CONTEXT, ctx);
	}

	public static void resetThreadLocal() {
		s_threadLocalContext.remove();
	}
}
