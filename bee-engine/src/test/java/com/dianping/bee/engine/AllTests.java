package com.dianping.bee.engine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.bee.engine.helper.SQLRegexTest;
import com.dianping.bee.engine.helper.SqlParsersTest;

@RunWith(Suite.class)
@SuiteClasses({

QueryServiceTest.class,

EvaluatorTest.class,

SqlParsersTest.class,

SQLRegexTest.class

})
public class AllTests {

}
