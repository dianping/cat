package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.analysis.AbstractMessageAnalyzerTest;
import com.dianping.cat.analysis.PeriodTaskTest;
import com.dianping.cat.server.ServerConfigVisitorTest;
import com.dianping.cat.service.DefaultReportManagerTest;
import com.dianping.cat.service.ModelPeriodTest;
import com.dianping.cat.service.ModelRequestTest;
import com.dianping.cat.service.ModelResponseTest;
import com.dianping.cat.statistic.ServerStatisticManagerTest;
import com.dianping.cat.storage.message.MessageBlockTest;
import com.dianping.cat.task.TaskManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

						MessageBlockTest.class,

/* .task */
						TaskManagerTest.class,

						ServerStatisticManagerTest.class,

						ModelRequestTest.class,

						ModelPeriodTest.class,

						ModelResponseTest.class,

						PeriodTaskTest.class,

						ServerConfigVisitorTest.class,

						AbstractMessageAnalyzerTest.class,

						DefaultReportManagerTest.class

})
public class AllTests {

}
