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
package com.dianping.cat.hadoop.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.message.storage.clean.HdfsUploader;
import org.unidal.cat.message.storage.clean.LogviewProcessor;
import org.unidal.cat.message.storage.hdfs.HdfsBucket;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.cat.message.storage.hdfs.HdfsFileBuilder;
import org.unidal.cat.message.storage.hdfs.HdfsIndex;
import org.unidal.cat.message.storage.hdfs.HdfsIndexManager;
import org.unidal.cat.message.storage.hdfs.HdfsMessageConsumerFinder;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMapping;
import org.unidal.cat.message.storage.hdfs.HdfsTokenMappingManager;
import org.unidal.cat.message.storage.internals.DefaultBlockDumper;
import org.unidal.cat.message.storage.internals.DefaultBlockDumperManager;
import org.unidal.cat.message.storage.internals.DefaultBlockWriter;
import org.unidal.cat.message.storage.internals.DefaultByteBufCache;
import org.unidal.cat.message.storage.internals.DefaultMessageDumper;
import org.unidal.cat.message.storage.internals.DefaultMessageDumperManager;
import org.unidal.cat.message.storage.internals.DefaultMessageFinderManager;
import org.unidal.cat.message.storage.internals.DefaultMessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultStorageConfiguration;
import org.unidal.cat.message.storage.local.LocalBucket;
import org.unidal.cat.message.storage.local.LocalBucketManager;
import org.unidal.cat.message.storage.local.LocalFileBuilder;
import org.unidal.cat.message.storage.local.LocalIndex;
import org.unidal.cat.message.storage.local.LocalIndexManager;
import org.unidal.cat.message.storage.local.LocalTokenMapping;
import org.unidal.cat.message.storage.local.LocalTokenMappingManager;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.CatHadoopModule;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.hadoop.hdfs.bucket.HarfsMessageBucket;
import com.dianping.cat.hadoop.hdfs.bucket.HdfsMessageBucket;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.MessageBucket;
import com.dianping.cat.message.storage.MessageBucketManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineLastStorage());
		all.addAll(defineLocalComponents());
		return all;
	}

	public List<Component> defineLastStorage() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(FileSystemManager.class) //
								.req(ServerConfigManager.class));

		all.add(C(Module.class, CatHadoopModule.ID, CatHadoopModule.class));

		all.add(C(MessageBucket.class, HdfsMessageBucket.ID, HdfsMessageBucket.class) //
								.is(PER_LOOKUP) //
								.req(FileSystemManager.class));

		all.add(C(MessageBucket.class, HarfsMessageBucket.ID, HarfsMessageBucket.class) //
								.is(PER_LOOKUP) //
								.req(FileSystemManager.class));

		all.add(C(MessageBucketManager.class, HdfsMessageBucketManager.ID, HdfsMessageBucketManager.class) //
								.req(FileSystemManager.class, ServerConfigManager.class) //
								.req(PathBuilder.class));

		return all;

	}

	public List<Component> defineLocalComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultMessageDumperManager.class));
		all.add(A(DefaultMessageFinderManager.class));
		all.add(A(DefaultMessageDumper.class));
		all.add(A(DefaultMessageProcessor.class));
		all.add(A(DefaultBlockDumperManager.class));
		all.add(A(DefaultBlockDumper.class));
		all.add(A(DefaultBlockWriter.class));

		all.add(A(HdfsSystemManager.class));

		all.add(A(HdfsMessageConsumerFinder.class));

		all.add(A(LocalBucket.class));
		all.add(A(LocalBucketManager.class));
		all.add(A(HdfsBucket.class));
		all.add(A(HdfsBucketManager.class));

		all.add(A(LocalIndex.class));
		all.add(A(LocalIndexManager.class));
		all.add(A(HdfsIndex.class));
		all.add(A(HdfsIndexManager.class));

		all.add(A(LocalFileBuilder.class));
		all.add(A(HdfsFileBuilder.class));
		all.add(A(LocalTokenMapping.class));
		all.add(A(HdfsTokenMapping.class));
		all.add(A(LocalTokenMappingManager.class));
		all.add(A(HdfsTokenMappingManager.class));

		all.add(A(DefaultStorageConfiguration.class));
		all.add(A(DefaultByteBufCache.class));

		all.add(A(HdfsUploader.class));
		all.add(A(LogviewProcessor.class));

		return all;
	}

}
