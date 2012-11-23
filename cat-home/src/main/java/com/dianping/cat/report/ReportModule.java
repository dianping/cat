package com.dianping.cat.report;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "r", defaultInboundAction = "home", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({

com.dianping.cat.report.page.home.Handler.class,

com.dianping.cat.report.page.problem.Handler.class,

com.dianping.cat.report.page.transaction.Handler.class,

com.dianping.cat.report.page.event.Handler.class,

com.dianping.cat.report.page.heartbeat.Handler.class,

com.dianping.cat.report.page.logview.Handler.class,

com.dianping.cat.report.page.ip.Handler.class,

com.dianping.cat.report.page.model.Handler.class,

com.dianping.cat.report.page.sql.Handler.class,

com.dianping.cat.report.page.heatmap.Handler.class,

com.dianping.cat.report.page.dashboard.Handler.class,

com.dianping.cat.report.page.task.Handler.class,

com.dianping.cat.report.page.matrix.Handler.class,

com.dianping.cat.report.page.health.Handler.class,

com.dianping.cat.report.page.cross.Handler.class,

com.dianping.cat.report.page.cache.Handler.class,

com.dianping.cat.report.page.database.Handler.class,

com.dianping.cat.report.page.state.Handler.class
})
public class ReportModule extends AbstractModule {

}
