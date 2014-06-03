package com.dianping.cat.broker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.broker.js.AggregationConfigManagerTest;
import com.dianping.cat.broker.js.ParseTest;

@RunWith(Suite.class)
@SuiteClasses({

AggregationConfigManagerTest.class,

ParseTest.class

})
public class AllTests {

}
