package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.problem.ProblemAnalyzerTest;
import com.dianping.cat.problem.ProblemCreateGraphDataTest;
import com.dianping.cat.problem.ProblemDailyGraphMergerTest;
import com.dianping.cat.problem.ProblemDailyGraphTest;
import com.dianping.cat.problem.ProblemFilterTest;
import com.dianping.cat.problem.ProblemGraphCreatorTest;
import com.dianping.cat.problem.ProblemGraphDataTest;
import com.dianping.cat.problem.ProblemHandlerTest;
import com.dianping.cat.problem.ProblemReportConvertorTest;
import com.dianping.cat.problem.ProblemReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

/* problem */
ProblemHandlerTest.class,

ProblemHandlerTest.class,

ProblemAnalyzerTest.class,

ProblemReportMergerTest.class,

ProblemFilterTest.class,

ProblemReportConvertorTest.class,

ProblemReportMergerTest.class,

ProblemGraphDataTest.class,

ProblemCreateGraphDataTest.class,

ProblemGraphCreatorTest.class,

ProblemDailyGraphMergerTest.class,

ProblemDailyGraphTest.class,

})
public class AllTests {

}
