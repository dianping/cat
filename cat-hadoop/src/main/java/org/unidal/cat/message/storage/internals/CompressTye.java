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
package org.unidal.cat.message.storage.internals;

public enum CompressTye {

	GZIP("gzip"),

	DEFLATE("deflate"),

	SNAPPY("snappy");

	private String m_name;

	private CompressTye(String name) {
		m_name = name;
	}

	public static CompressTye getCompressTye(String name) {
		for (CompressTye type : values()) {
			if (name.equals(type.getName())) {
				return type;
			}
		}
		return GZIP;
	}

	public String getName() {
		return m_name;
	}

}
