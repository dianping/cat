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
package com.dianping.cat.message.storage;

import java.util.ArrayList;
import java.util.List;

public class MessageBlock {
	private String m_dataFile;

	private byte[] m_data;

	private List<Integer> m_indexes = new ArrayList<Integer>(32);

	private List<Integer> m_sizes = new ArrayList<Integer>(32);

	public MessageBlock(String dataFile) {
		m_dataFile = dataFile;
	}

	public void addIndex(int index, int size) {
		m_indexes.add(index);
		m_sizes.add(size);
	}

	public int getBlockSize() {
		return m_indexes.size();
	}

	public byte[] getData() {
		return m_data;
	}

	public void setData(byte[] data) {
		m_data = data;
	}

	public String getDataFile() {
		return m_dataFile;
	}

	public int getIndex(int index) {
		return m_indexes.get(index);
	}

	public int getSize(int index) {
		return m_sizes.get(index);
	}
}
