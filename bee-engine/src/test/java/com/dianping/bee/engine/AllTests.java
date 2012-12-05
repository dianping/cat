package com.dianping.bee.engine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.bee.engine.helper.SQLWildcardTest;
import com.dianping.bee.engine.helper.SqlParsersTest;
import com.dianping.bee.jdbc.AllJDBCTests;

@RunWith(Suite.class)
@SuiteClasses({

QueryServiceTest.class,

LogicEvaluatorTest.class,

FunctionEvaluatorTest.class,

SqlParsersTest.class,

SQLWildcardTest.class,

ExceptionTest.class,

AllJDBCTests.class

})
public class AllTests {

}
