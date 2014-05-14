package com.dianping.cat.broker.api;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "api", defaultInboundAction = "signal", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.broker.api.page.signal.Handler.class,

com.dianping.cat.broker.api.page.batch.Handler.class
})
public class ApiModule extends AbstractModule {

}
