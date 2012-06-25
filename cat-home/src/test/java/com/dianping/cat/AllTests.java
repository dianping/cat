package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.graph.ValueTranslaterTest;
import com.dianping.cat.report.page.ip.DisplayModelTest;
import com.dianping.cat.report.page.ip.location.IPSeekerTest;
import com.dianping.cat.report.page.model.EventReportFilterTest;
import com.dianping.cat.report.page.model.TransactionReportFilterTest;
import com.dianping.cat.report.page.model.event.EventModelServiceTest;
import com.dianping.cat.report.page.model.transaction.TransactionModelServiceTest;
import com.dianping.cat.report.page.sql.TestComputeStr;
import com.dianping.cat.report.page.transaction.GraphDateTest;
import com.dianping.cat.report.page.transaction.PayloadTest;
import com.dianping.cat.report.page.transaction.TransactionReportMergerTest;
import com.dianping.cat.report.task.TaskConsumerTest;
import com.dianping.cat.report.task.TaskHelperTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.page.model.event */
EventModelServiceTest.class,

/* .report.page.model.transaction */
TransactionModelServiceTest.class,

/* .report.page.transaction */
TransactionReportMergerTest.class,

/* .report.graph */
ValueTranslaterTest.class,

/* .report.page.ip */
DisplayModelTest.class, IPSeekerTest.class,

/* .report.page.model */
EventReportFilterTest.class, TransactionReportFilterTest.class,

TestComputeStr.class,

/* . report.page.transcation */
GraphDateTest.class, PayloadTest.class, TransactionReportMergerTest.class,

/* .report.task */
TaskConsumerTest.class, TaskHelperTest.class })
public class AllTests {

}
