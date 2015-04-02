package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.analyzer.matrix.MatrixAnalyzerTest;
import com.dianping.cat.analyzer.matrix.MatrixModelTest;
import com.dianping.cat.analyzer.matrix.MatrixReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

MatrixModelTest.class,

MatrixReportMergerTest.class,

MatrixAnalyzerTest.class

// add test classes here

})
public class AllTests {

}
