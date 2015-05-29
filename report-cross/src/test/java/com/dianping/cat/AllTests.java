package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.cross.CrossAnalyzerTest;
import com.dianping.cat.cross.CrossInfoTest;
import com.dianping.cat.cross.CrossReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

CrossInfoTest.class,

CrossReportMergerTest.class,

CrossAnalyzerTest.class

})
public class AllTests {

}
