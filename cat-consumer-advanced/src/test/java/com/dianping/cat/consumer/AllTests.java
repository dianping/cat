package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.cross.ParseCrossInfoTest;
import com.dianping.cat.consumer.database.DatabaseAnalyzerTest;
import com.dianping.cat.consumer.ip.IpReportTest;
import com.dianping.cat.consumer.matrix.MatrixReportFilterTest;
import com.dianping.cat.consumer.sql.SqlParsersTest;

@RunWith(Suite.class)
@SuiteClasses({

ParseCrossInfoTest.class,

DatabaseAnalyzerTest.class,

IpReportTest.class,

MatrixReportFilterTest.class,

SqlParsersTest.class

})
public class AllTests {

}
