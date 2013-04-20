package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.cross.ParseCrossInfoTest;
import com.dianping.cat.consumer.database.DatabaseAnalyzerTest;
import com.dianping.cat.consumer.ip.IpReportTest;
import com.dianping.cat.consumer.matrix.MatrixReportFilterTest;

@RunWith(Suite.class)
@SuiteClasses({

IpReportTest.class,

MatrixReportFilterTest.class,

ParseCrossInfoTest.class,

DatabaseAnalyzerTest.class

})
public class AllTests {

}
