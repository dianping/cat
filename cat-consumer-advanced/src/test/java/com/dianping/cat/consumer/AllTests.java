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
import com.dianping.cat.consumer.metric.ProductLineConfigManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

MetricAnalyzerTest.class,

CrossInfoTest.class,

CrossReportMergerTest.class,

MatrixModelTest.class,

MatrixReportMergerTest.class,

CrossAnalyzerTest.class,

MatrixAnalyzerTest.class,

DependencyAnalyzerTest.class,

DependencyReportMergerTest.class,

ProductLineConfigManagerTest.class,

})
public class AllTests {

}
