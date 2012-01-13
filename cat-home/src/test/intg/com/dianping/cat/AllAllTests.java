package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

com.dianping.cat.message.AllTests.class,

com.dianping.cat.consumer.AllTests.class,

})
public class AllAllTests {

}
