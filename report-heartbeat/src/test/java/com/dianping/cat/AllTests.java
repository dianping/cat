package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.heartbeat.HeartbeatAnalyzerTest;
import com.dianping.cat.heartbeat.HeartbeatDailyMergerTest;
import com.dianping.cat.heartbeat.HeartbeatReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

/* heartbeat */
HeartbeatAnalyzerTest.class,

HeartbeatReportMergerTest.class,

HeartbeatDailyMergerTest.class,

})
public class AllTests {

}
