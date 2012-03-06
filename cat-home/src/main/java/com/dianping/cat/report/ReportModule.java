package com.dianping.cat.report;

import com.site.web.mvc.AbstractModule;
import com.site.web.mvc.annotation.ModuleMeta;
import com.site.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "r", defaultInboundAction = "home", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.report.page.home.Handler.class,

com.dianping.cat.report.page.transaction.Handler.class,

com.dianping.cat.report.page.failure.Handler.class,

com.dianping.cat.report.page.logview.Handler.class,

com.dianping.cat.report.page.ip.Handler.class,

com.dianping.cat.report.page.service.Handler.class,

com.dianping.cat.report.page.model.Handler.class,

com.dianping.cat.report.page.problem.Handler.class
})
public class ReportModule extends AbstractModule {

}
