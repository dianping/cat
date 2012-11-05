package com.dianping.cat.system;

import com.site.web.mvc.AbstractModule;
import com.site.web.mvc.annotation.ModuleMeta;
import com.site.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "s", defaultInboundAction = "alarm", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.system.page.alarm.Handler.class,

com.dianping.cat.system.page.login.Handler.class,

com.dianping.cat.system.page.project.Handler.class
})
public class SystemModule extends AbstractModule {

}
