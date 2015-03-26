package com.dianping.cat.broker.api.page.crash;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.ApiPage;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class Handler implements PageHandler<Context> {

	public static final String ANDRIOD = "AndroidCrashLog";

	public static final String IPHONE = "iOSCrashLog";

	public static final char SPIT = ':';

	public String buildDomain(Payload payload) {
		int type = payload.getMobileType();

		if (type == Payload.ANDRIOD) {
			return ANDRIOD;
		} else {
			return IPHONE;
		}
	}

	public String buildIp(Payload payload) {
		String appVerion = String.valueOf(payload.getAppVersion());
		String plateformVersion = String.valueOf(payload.getPlateformVersion());
		String module = String.valueOf(payload.getModule());
		String level = String.valueOf(payload.getLevel());

		return appVerion + SPIT + plateformVersion + SPIT + module + SPIT + level;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "crash")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "crash")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ApiPage.CRASH);

		String domain = buildDomain(payload);
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		Transaction t = Cat.newTransaction("CrashLog", domain);

		Cat.logEvent("Error", payload.getMessage(), "ERROR", payload.getDetail());
		((DefaultMessageTree) tree).setIpAddress(buildIp(payload));
		((DefaultMessageTree) tree).setDomain(domain);

		t.setStatus(Transaction.SUCCESS);
		t.complete();

		ctx.getHttpServletResponse().getWriter().write("OK");
	}
}
