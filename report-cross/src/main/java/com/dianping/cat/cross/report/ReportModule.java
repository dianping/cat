package com.dianping.cat.cross.report;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "r", defaultInboundAction = "cross", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.cross.Handler.class
})
public class ReportModule extends AbstractModule {

}
