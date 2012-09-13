package com.dianping.bee.engine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.bee.engine.helper.SQLWildcardTest;
import com.dianping.bee.engine.helper.SqlParsersTest;

@RunWith(Suite.class)
@SuiteClasses({

QueryServiceTest.class,

EvaluatorTest.class,

SqlParsersTest.class,

SQLWildcardTest.class,

ExceptionTest.class

})
public class AllTests {

}
