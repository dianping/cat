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
package com.dianping.cat.report.alert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.report.alert.spi.config.BaseRuleConfigManager;

public class MetricIdAndRuleMappingTest {

	private String filePath = "/config/test-network.xml";

	private String[] texts = { "f5-2400-1-dianping-com:metric:1/1/1inerrors",
							"switch-SH-HM-C3750G:metric:GigabitEthernet2/0/1-in", "f5-2400-1-dianping-com:metric:1/1-2-out",
							"f5-2400-1-dianping-com:metric:1/1-1-out", "f5-2400-1-dianping-com:metric:1/1-8-out" };

	private BaseRuleConfigManager m_manager = new BaseRuleConfigManager() {
		@Override
		protected String getConfigName() {
			return null;
		}
	};

	private List<String> buildPatternList(String path) {
		try {
			String content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
			return Arrays.asList(content.split("[\r\n]+"));
		} catch (IOException e) {
			return null;
		}
	}

	private int findTextByPatterns(String text, List<String> patterns) {
		int tmpResult = 0;

		for (String pattern : patterns) {
			tmpResult = m_manager.validate(null, pattern, null, text);

			if (tmpResult > 0) {
				return tmpResult;
			}
		}

		return tmpResult;
	}

	@Test
	public void test() {
		List<String> patterns = buildPatternList(filePath);

		for (String text : texts) {
			Assert.assertTrue(findTextByPatterns(text, patterns) > 0);
		}
	}
}
