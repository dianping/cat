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
package com.dianping.cat.config;

import java.util.HashMap;
import java.util.Map;

public class TrieTreeNode {

	/**
		* tree index
		*/
	private Map<TrieTreeKey, TrieTreeNode> m_childMap = new HashMap<TrieTreeKey, TrieTreeNode>();

	/**
		* data of node
		*/
	private Map<String, AggregationMessageFormat> m_formatMap = new HashMap<String, AggregationMessageFormat>();

	public void addFormat(String key, AggregationMessageFormat format) {
		m_formatMap.put(key, format);
	}

	public void addTreeNode(char ch, boolean isPrefix, TrieTreeNode node) {
		TrieTreeKey key = new TrieTreeKey(ch, isPrefix);
		m_childMap.put(key, node);
	}

	public TrieTreeNode getChildNode(char ch, boolean isPrefix) {
		TrieTreeKey key = new TrieTreeKey(ch, isPrefix);
		return m_childMap.get(key);
	}

	public Map<String, AggregationMessageFormat> getFormatMap() {
		return m_formatMap;
	}

	@Override
	public String toString() {
		return "TrieTreeNode [m_childMap=" + m_childMap + ", m_formatList=" + m_formatMap + "]";
	}

	/**
		* Used as trie tree index
		*
		* @author renyuan.sun
		*/
	class TrieTreeKey {
		/**
			* character index
			*/
		char m_ch;

		/**
			* true when the character is prefix false when the character is suffix
			*/
		boolean m_isPrefix;

		public TrieTreeKey(char ch, boolean isPrefix) {
			super();
			this.m_ch = ch;
			this.m_isPrefix = isPrefix;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TrieTreeKey other = (TrieTreeKey) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (m_ch != other.m_ch) {
				return false;
			}
			if (m_isPrefix != other.m_isPrefix) {
				return false;
			}
			return true;
		}

		private TrieTreeNode getOuterType() {
			return TrieTreeNode.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + m_ch;
			result = prime * result + (m_isPrefix ? 1231 : 1237);
			return result;
		}

		@Override
		public String toString() {
			return "TrieTreeKey [m_ch=" + m_ch + ", m_isPrefix=" + m_isPrefix + "]";
		}
	}

}
