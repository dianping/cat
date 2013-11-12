package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.advanced.CrossInfoTest;
import com.dianping.cat.consumer.advanced.DatabaseParserTest;
import com.dianping.cat.consumer.advanced.MatrixModelTest;
import com.dianping.cat.consumer.advanced.SqlParsersTest;
import com.dianping.cat.consumer.browser.BrowserReportMergerTest;
import com.dianping.cat.consumer.cross.CrossAnalyzerTest;
import com.dianping.cat.consumer.cross.CrossReportMergerTest;
import com.dianping.cat.consumer.dependency.DependencyAnalyzerTest;
import com.dianping.cat.consumer.dependency.DependencyReportMergerTest;
import com.dianping.cat.consumer.matrix.MatrixAnalyzerTest;
import com.dianping.cat.consumer.matrix.MatrixReportMergerTest;
import com.dianping.cat.consumer.sql.SqlAnalyzerTest;
import com.dianping.cat.consumer.sql.SqlReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

CrossInfoTest.class,

CrossReportMergerTest.class,

MatrixModelTest.class,

MatrixReportMergerTest.class,

SqlParsersTest.class,

SqlReportMergerTest.class,

DatabaseParserTest.class,

BrowserReportMergerTest.class,

CrossAnalyzerTest.class,

SqlAnalyzerTest.class,

MatrixAnalyzerTest.class,

DependencyAnalyzerTest.class,

DependencyReportMergerTest.class

})
public class AllTests {

}
