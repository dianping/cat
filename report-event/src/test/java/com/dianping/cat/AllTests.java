package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.event.EventAnalyzerTest;
import com.dianping.cat.event.EventDailyGraphMergerTest;
import com.dianping.cat.event.EventGraphCreatorTest;
import com.dianping.cat.event.EventGraphDataTest;
import com.dianping.cat.event.EventReportFilterTest;
import com.dianping.cat.event.EventReportMergerTest;
import com.dianping.cat.event.HistoryEventMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

EventAnalyzerTest.class,

EventReportMergerTest.class,

EventReportFilterTest.class,

EventGraphDataTest.class,

HistoryEventMergerTest.class,

EventGraphCreatorTest.class,

EventDailyGraphMergerTest.class,

})
public class AllTests {

}
