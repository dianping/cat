package com.dianping.cat.report.heartbeat.heartbeat;

import com.site.web.mvc.AbstractModule;
import com.site.web.mvc.annotation.ModuleMeta;
import com.site.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "H", defaultInboundAction = "heartbeat", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.report.heartbeat.heartbeat.page.heartbeat.Handler.class
})
public class HeartbeatModule extends AbstractModule {

}
