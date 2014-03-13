package com.dianping.cat.hadoop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

HdfsMessageBucketManagerTest.class

})
public class AllTests {

}
