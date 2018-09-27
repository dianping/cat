package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.message.EventTest;
import com.dianping.cat.message.HeartbeatTest;
import com.dianping.cat.message.TransactionTest;
import com.dianping.cat.message.internal.MessageIdFactoryTest;
import com.dianping.cat.message.internal.MockMessageBuilderTest;
import com.dianping.cat.message.internal.MultiThreadingTest;
import com.dianping.cat.servlet.CatFilterTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .message */

						EventTest.class,

						HeartbeatTest.class,

						TransactionTest.class,

						MockMessageBuilderTest.class,

/* .servlet */
						CatFilterTest.class,

/* .tool */
						ToolsTest.class,

						CatTest.class,

						MessageIdFactoryTest.class,

						MockMessageBuilderTest.class,

						MultiThreadingTest.class

})
public class AllTests {

}
