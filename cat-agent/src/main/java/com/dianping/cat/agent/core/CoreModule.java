package com.dianping.cat.agent.core;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "core", defaultInboundAction = "index", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.agent.core.page.index.Handler.class
})
public class CoreModule extends AbstractModule {

}
