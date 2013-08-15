package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.advanced.CrossInfoTest;
import com.dianping.cat.consumer.advanced.DatabaseParserTest;
import com.dianping.cat.consumer.advanced.MatrixModelTest;
import com.dianping.cat.consumer.advanced.SqlParsersTest;

@RunWith(Suite.class)
@SuiteClasses({

CrossInfoTest.class,

MatrixModelTest.class,

SqlParsersTest.class,

DatabaseParserTest.class

})
public class AllTests {

}
