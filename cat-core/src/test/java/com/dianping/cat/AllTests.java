package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.analysis.AbstractMessageAnalyzerTest;
import com.dianping.cat.analysis.PeriodTaskTest;
import com.dianping.cat.config.CommandFormatManagerTest;
import com.dianping.cat.message.codec.HtmlMessageCodecTest;
import com.dianping.cat.message.codec.WaterfallMessageCodecTest;
import com.dianping.cat.server.ServerConfigManagerTest;
import com.dianping.cat.service.DefaultReportManagerTest;
import com.dianping.cat.service.ModelPeriodTest;
import com.dianping.cat.service.ModelRequestTest;
import com.dianping.cat.service.ModelResponseTest;
import com.dianping.cat.statistic.ServerStatisticManagerTest;
import com.dianping.cat.storage.message.LocalMessageBucketTest;
import com.dianping.cat.storage.message.MessageBlockTest;
import com.dianping.cat.task.TaskManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

HtmlMessageCodecTest.class,

WaterfallMessageCodecTest.class,

/* .storage.dump */
LocalMessageBucketTest.class,

MessageBlockTest.class,

/* .task */
TaskManagerTest.class,

ServerStatisticManagerTest.class,

ModelRequestTest.class,

ModelPeriodTest.class,

ModelResponseTest.class,

PeriodTaskTest.class,

ServerConfigManagerTest.class,

AbstractMessageAnalyzerTest.class,

DefaultReportManagerTest.class,

CommandFormatManagerTest.class

})
public class AllTests {

}
