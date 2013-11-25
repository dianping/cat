package com.dianping.cat.abtest.conditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.Condition;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.system.page.abtest.conditions.ScriptProvider;
import com.dianping.cat.system.page.abtest.conditions.URLScriptProvider;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class TrafficFilterCodeGenerationTest {

	private Configuration m_configuration;

	private URLScriptProvider m_urlScriptProvider;

	private Run m_run;

	@Before
	public void init() {
		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(TrafficFilterCodeGenerationTest.class, "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}

		m_urlScriptProvider = new URLScriptProvider();
		m_run = new Run();

		List<Condition> conditions = m_run.getConditions();

		conditions.add(getCondition("url", 1, "http://www.DIANPING.com", 1, "and"));
		conditions.add(getCondition("url", 5, "http://www.dianping.com/*", 2, "and"));
		conditions.add(getCondition("url", 8, "http://www.dianping.com1", 3, "and"));
		conditions.add(getCondition("url", 3, "http://www.dianping.com2", 4, "or"));
		conditions.add(getCondition("url", 4, "http://www.dianping.com/2", 4, "and"));
		conditions.add(getCondition("url", 6, "http://www.dianping.com/*", 4, "and"));
		conditions.add(getCondition("percent", 6, "100", 5, "and"));
	}

	public Condition getCondition(String name, int comparator, String text, int seq, String operator) {
		Condition condition = new Condition();

		condition.setComparator(comparator);
		condition.setName(name);
		condition.setSeq(seq);
		condition.setOperator(operator);
		condition.setText(text);

		return condition;
	}

	@Test
	public void test() throws IOException {
		Map<Object, Object> root = new HashMap<Object, Object>();

		root.put("run", m_run);
		root.put("urlScriptProvider", m_urlScriptProvider);

		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate(ScriptProvider.m_fileName);

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
		}

		InputStream inputstream = getClass().getResourceAsStream("TrafficFilter");
		String expected = Files.forIO().readFrom(inputstream, "utf-8");

		Assert.assertEquals(expected, sw.toString());
	}
}
