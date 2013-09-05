package com.dianping.cat.home.abtest;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.Condition;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.system.page.abtest.conditions.URLScriptProvider;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ScriptFragementTest{
	
	private Configuration m_configuration;
	
	private URLScriptProvider m_urlScriptProvider;
	
	private Run m_run;

	@Before
   public void init() {
		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(ScriptFragementTest.class, "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}
		
		m_urlScriptProvider = new URLScriptProvider();
		m_run = new Run();
		
		List<Condition> conditions = m_run.getConditions();
		
		conditions.add(getCondition(1, "url", 1, "and", "http://www.dianping.com"));
		conditions.add(getCondition(5, "url", 2, "and", "http://www.dianping.com/*"));
		conditions.add(getCondition(2, "url", 3, "and", "http://www.dianping.com1"));
		conditions.add(getCondition(3, "url", 4, "and", "http://www.dianping.com2"));
		conditions.add(getCondition(4, "url", 4, "or", "http://www.dianping.com3"));
		conditions.add(getCondition(6, "percent", 5, "and", "100"));
	}
	
	public Condition getCondition(int comparator, String name, int seq, String operator, String text){
		Condition condition = new Condition();
		
		condition.setComparator(comparator);
		condition.setName(name);
		condition.setSeq(seq);
		condition.setOperator(operator);
		condition.setText(text);
		
		return condition;
	}
	
	@Test
	public void test(){
		Map<Object, Object> root = new HashMap<Object, Object>();
		
		root.put("run", m_run);
		root.put("urlScriptProvider", m_urlScriptProvider);
		
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("scriptFragement.ftl");

			t.process(root, sw);
		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
		}
		
		
		System.out.println(sw.toString());
	}

}
