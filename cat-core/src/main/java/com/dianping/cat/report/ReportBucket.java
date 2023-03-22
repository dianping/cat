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
package com.dianping.cat.report;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

public interface ReportBucket {
	/**
		* Close bucket and release component instance
		*
		* @throws IOException
		*/
	public void close() throws IOException;

	/**
		* Find data by given id in the bucket. return null if not found.
		*
		* @param id
		* @return data for given id, null if not found
		* @throws IOException
		*/
	public String findById(String id) throws IOException;

	/**
		* Flush the buffered data in the bucket if have.
		*
		* @throws IOException
		*/
	public void flush() throws IOException;

	/**
		* Return all ids in the bucket.
		*
		* @return
		*/
	public Collection<String> getIds();

	/**
		* Initialize the bucket after its creation.
		*
		* @param name
		* @param timestamp
		* @param index
		* @throws IOException
		*/
	public void initialize(String name, Date timestamp, int index) throws IOException;

	/**
		* store the data by id into the bucket.
		*
		* @param id
		* @param data
		* @return true means the data was stored in the bucket, otherwise false.
		* @throws IOException
		*/
	public boolean storeById(String id, String data) throws IOException;

}
