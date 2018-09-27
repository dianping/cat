package com.dianping.cat.report;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "r", defaultInboundAction = "top", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.report.page.home.Handler.class,

com.dianping.cat.report.page.problem.Handler.class,

com.dianping.cat.report.page.transaction.Handler.class,

com.dianping.cat.report.page.event.Handler.class,

com.dianping.cat.report.page.heartbeat.Handler.class,

com.dianping.cat.report.page.logview.Handler.class,

com.dianping.cat.report.page.model.Handler.class,

com.dianping.cat.report.page.matrix.Handler.class,

com.dianping.cat.report.page.cross.Handler.class,

com.dianping.cat.report.page.cache.Handler.class,

com.dianping.cat.report.page.state.Handler.class,

com.dianping.cat.report.page.statistics.Handler.class,

com.dianping.cat.report.page.alteration.Handler.class,

com.dianping.cat.report.page.monitor.Handler.class,

com.dianping.cat.report.page.alert.Handler.class,

com.dianping.cat.report.page.overload.Handler.class,

com.dianping.cat.report.page.storage.Handler.class,

com.dianping.cat.report.page.top.Handler.class,

com.dianping.cat.report.page.business.Handler.class,

})
public class ReportModule extends AbstractModule {

}
