package com.dianping.cat.system.page.abtest.handler;

import com.dianping.cat.system.page.abtest.Context;
import com.dianping.cat.system.page.abtest.Model;
import com.dianping.cat.system.page.abtest.Payload;

public interface SubHandler {
	public void handleInbound(Context ctx, Payload payload);

	public void handleOutbound(Context ctx, Model model, Payload payload);
}
