package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.matrix.analyzer.MatrixAnalyzerTest;
import com.dianping.cat.matrix.analyzer.MatrixModelTest;
import com.dianping.cat.matrix.analyzer.MatrixReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

MatrixModelTest.class,

MatrixReportMergerTest.class,

MatrixAnalyzerTest.class

// add test classes here

})
public class AllTests {

}
