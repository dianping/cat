package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.message.spi.core.HtmlMessageCodecTest;
import com.dianping.cat.storage.dump.LocalMessageBucketManagerTest;
import com.dianping.cat.storage.dump.LocalMessageBucketTest;
import com.dianping.cat.storage.report.LocalReportBucketTest;
import com.dianping.cat.task.TaskManager;

@RunWith(Suite.class)
@SuiteClasses({

HtmlMessageCodecTest.class,

/* .storage */
LocalReportBucketTest.class,

/* .storage.dump */
LocalMessageBucketTest.class,

LocalMessageBucketManagerTest.class,

TaskManager.class

})
public class AllTests {

}
