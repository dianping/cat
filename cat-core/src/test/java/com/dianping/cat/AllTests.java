package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.log4j.CatAppenderTest;
import com.dianping.cat.message.EventTest;
import com.dianping.cat.message.HeartbeatTest;
import com.dianping.cat.message.TransactionTest;
import com.dianping.cat.message.configuration.ClientConfigTest;
import com.dianping.cat.message.internal.MessageIdFactoryTest;
import com.dianping.cat.message.internal.MillisSecondTimerTest;
import com.dianping.cat.message.io.TcpSocketTest;
import com.dianping.cat.message.spi.codec.HtmlMessageCodecTest;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest;
import com.dianping.cat.message.spi.internal.DefaultMessagePathBuilderTest;
import com.dianping.cat.storage.BucketManagerTest;
import com.dianping.cat.storage.message.LocalLogviewBucketTest;
import com.dianping.cat.storage.message.LogviewBucketTest;
import com.dianping.cat.storage.report.LocalReportBucketTest;

@RunWith(Suite.class)
@SuiteClasses({

/* <default> */
CatTest.class,

/* .log4j */
CatAppenderTest.class,

/* .message */
EventTest.class,

HeartbeatTest.class,

TransactionTest.class,

/* .configuration.model */
ClientConfigTest.class,

/* .internal */
MessageIdFactoryTest.class,

MillisSecondTimerTest.class,

DefaultMessagePathBuilderTest.class,

/* .io */
TcpSocketTest.class,

/* .spi.codec */
PlainTextMessageCodecTest.class,

HtmlMessageCodecTest.class,

/* .storage */
BucketManagerTest.class,

LocalReportBucketTest.class,

LocalLogviewBucketTest.class,

/* .storage.message */
LogviewBucketTest.class

})
public class AllTests {

}
