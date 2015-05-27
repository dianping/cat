package com.dianping.cat.problem.report;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "r", defaultInboundAction = "problem", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.problem.Handler.class
})
public class ReportModule extends AbstractModule {

}
