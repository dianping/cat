package com.dianping.cat.hadoop;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.hadoop.hdfs.FileSystemManagerTest;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManagerTest;
import com.dianping.cat.hadoop.sql.SqlJobDataProduceTest;

@RunWith(Suite.class)
@SuiteClasses({

FileSystemManagerTest.class,

HdfsMessageBucketManagerTest.class,

SqlJobDataProduceTest.class

})
public class AllTests {

}
