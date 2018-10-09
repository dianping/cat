package org.unidal.cat.message;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.message.storage.IndexManagerTest;
import org.unidal.cat.message.storage.IndexTest;
import org.unidal.cat.message.storage.TokenMappingTest;

@RunWith(Suite.class)
@SuiteClasses({

MessageIdTest.class,

BenchmarkTest.class,

IndexManagerTest.class,

IndexTest.class,

TokenMappingTest.class,

})
public class AllMessageTests {

}
