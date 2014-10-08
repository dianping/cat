package com.dianping.cat.report.task.alert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.system.config.BaseRuleConfigManager;

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
			return Arrays.asList(content.split("\n"));
		} catch (IOException e) {
			return null;
		}
	}

	private boolean findTextByPatterns(String text, List<String> patterns) {
		boolean tmpResult = false;

		for (String pattern : patterns) {
			tmpResult = m_manager.validate(null, pattern, null, text);

			if (tmpResult) {
				return tmpResult;
			}
		}

		return tmpResult;
	}

	@Test
	public void test() {
		List<String> patterns = buildPatternList(filePath);

		for (String text : texts) {
			Assert.assertEquals(findTextByPatterns(text, patterns), true);
		}
	}
}
