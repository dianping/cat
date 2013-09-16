package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.configuration.ConfigTest;
import com.dianping.cat.log4j.CatAppenderTest;
import com.dianping.cat.message.EventTest;
import com.dianping.cat.message.HeartbeatTest;
import com.dianping.cat.message.MessageTest;
import com.dianping.cat.message.TransactionTest;
import com.dianping.cat.message.internal.MessageIdFactoryTest;
import com.dianping.cat.message.internal.MillisSecondTimerTest;
import com.dianping.cat.message.internal.MockMessageBuilderTest;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest;
import com.dianping.cat.message.spi.internal.DefaultMessagePathBuilderTest;
import com.dianping.cat.servlet.CatFilterTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .log4j */
CatAppenderTest.class,

/* .message */
MessageTest.class,

EventTest.class,

HeartbeatTest.class,

TransactionTest.class,

/* .configuration */
ConfigTest.class,

/* .internal */
MessageIdFactoryTest.class,

MillisSecondTimerTest.class,

DefaultMessagePathBuilderTest.class,

MockMessageBuilderTest.class,

/* .spi.codec */
PlainTextMessageCodecTest.class,

/* .servlet */
CatFilterTest.class

})
public class AllTests {

}
