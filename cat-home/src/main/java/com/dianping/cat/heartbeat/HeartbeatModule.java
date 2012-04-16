package com.dianping.cat.heartbeat;

import com.site.web.mvc.AbstractModule;
import com.site.web.mvc.annotation.ModuleMeta;
import com.site.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "h", defaultInboundAction = "heartbeat", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.report.page.heartbeat.Handler.class
})
public class HeartbeatModule extends AbstractModule {

}
