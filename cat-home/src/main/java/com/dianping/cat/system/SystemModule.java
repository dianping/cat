package com.dianping.cat.system;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "s", defaultInboundAction = "alarm", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.system.page.alarm.Handler.class,

com.dianping.cat.system.page.login.Handler.class,

com.dianping.cat.system.page.config.Handler.class
})
public class SystemModule extends AbstractModule {

}
