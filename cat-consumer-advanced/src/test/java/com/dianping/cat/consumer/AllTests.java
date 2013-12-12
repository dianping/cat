package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.cross.CrossAnalyzerTest;
import com.dianping.cat.consumer.cross.CrossInfoTest;
import com.dianping.cat.consumer.cross.CrossReportMergerTest;
import com.dianping.cat.consumer.dependency.DependencyAnalyzerTest;
import com.dianping.cat.consumer.dependency.DependencyReportMergerTest;
import com.dianping.cat.consumer.matrix.MatrixAnalyzerTest;
import com.dianping.cat.consumer.matrix.MatrixModelTest;
import com.dianping.cat.consumer.matrix.MatrixReportMergerTest;
import com.dianping.cat.consumer.metric.MetricAnalyzerTest;
import com.dianping.cat.consumer.metric.MetricConfigManagerTest;
import com.dianping.cat.consumer.metric.ProductLineConfigManagerTest;
import com.dianping.cat.consumer.sql.DatabaseParserTest;
import com.dianping.cat.consumer.sql.SqlAnalyzerTest;
import com.dianping.cat.consumer.sql.SqlParsersTest;
import com.dianping.cat.consumer.sql.SqlReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

MetricAnalyzerTest.class,

CrossInfoTest.class,

CrossReportMergerTest.class,

MatrixModelTest.class,

MatrixReportMergerTest.class,

DatabaseParserTest.class,

CrossAnalyzerTest.class,

SqlAnalyzerTest.class,

SqlParsersTest.class,

SqlReportMergerTest.class,

MatrixAnalyzerTest.class,

DependencyAnalyzerTest.class,

DependencyReportMergerTest.class,

ProductLineConfigManagerTest.class,

MetricConfigManagerTest.class

})
public class AllTests {

}
