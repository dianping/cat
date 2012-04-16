package com.dianping.cat.report.heartbeat.heartbeat;

import com.site.web.mvc.Action;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.Page;

public class HeartbeatContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {

}
