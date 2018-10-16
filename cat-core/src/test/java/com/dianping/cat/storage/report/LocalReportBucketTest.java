/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.storage.report;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;

import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.storage.StringBucketTestCase;

public class LocalReportBucketTest extends StringBucketTestCase {

	@Override
	protected ReportBucket createBucket() throws Exception, IOException {
		ReportBucket bucket = lookup(ReportBucket.class, String.class.getName() + "-report");
		bucket.initialize("cat", new Date(), 0);
		return bucket;
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		String m_baseDir = ((LocalReportBucket) this.bucket).getBaseDir();
		String logicalPath = ((LocalReportBucket) this.bucket).getLogicalPath();
		new File(m_baseDir, logicalPath).delete();
		new File(m_baseDir, logicalPath + ".idx").delete();
	}

}
