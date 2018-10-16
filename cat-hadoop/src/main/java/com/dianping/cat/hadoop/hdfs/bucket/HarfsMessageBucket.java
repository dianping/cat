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
package com.dianping.cat.hadoop.hdfs.bucket;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.MessageBlockReader;

public class HarfsMessageBucket extends AbstractHdfsMessageBucket {

	public static final String ID = HdfsMessageBucketManager.HARFS_BUCKET;

	@Override
	public void initialize(String dataFile) throws IOException {
		initialize(dataFile, new Date());
	}

	@Override
	public void initialize(String dataFile, Date date) throws IOException {
		FileSystem fs = m_manager.getHarFileSystem(m_id, date);
		int index = dataFile.indexOf("/");

		if (index > 0) {
			String parent = dataFile.substring(0, index);
			dataFile = dataFile.substring(index + 1);
			Path basePath = new Path(parent);
			m_reader = new MessageBlockReader(fs, basePath, dataFile);
		} else {
			m_reader = new MessageBlockReader(fs, dataFile);
		}
	}

}
