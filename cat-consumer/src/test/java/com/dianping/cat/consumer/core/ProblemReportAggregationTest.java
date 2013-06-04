package com.dianping.cat.consumer.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dainping.cat.consumer.core.dal.AggregationRule;
import com.dianping.cat.consumer.core.aggregation.AggregationManager;
import com.dianping.cat.consumer.core.aggregation.DefaultAggregationHandler;
import com.dianping.cat.consumer.core.problem.ProblemReportAggregation;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportAggregationTest {

	@Test
	public void testAggregation() throws IOException, SAXException {
		ProblemReportAggregation aggregation = new ProblemReportAggregation();
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report-before-aggregation.xml"),
		      "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report-after-aggregation.xml"),
		      "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		MockRuleManger ruleManger = new MockRuleManger();
		ruleManger.register();
		aggregation.setRuleManger(ruleManger);
		long start = (new Date()).getTime();
		for (int i = 0; i < 1000; i++) {
			aggregation.visitProblemReport(reportOld);
		}
		System.out.println(((new Date()).getTime()-start)*1.0/1000);
		Assert.assertEquals(reportNew.toString().replaceAll("\r", ""),
		      aggregation.getReport().toString().replaceAll("\r", ""));
	}

	class MockRuleManger extends AggregationManager {
		private void register(){
			List<AggregationRule> rules = getAggregationRule(AggregationManager.PROBLEM_TYPE, "FrontEnd");
			Map<Integer, Map<String, List<AggregationRule>>> ruleMap = new HashMap<Integer, Map<String, List<AggregationRule>>>();
			Map<String, List<AggregationRule>> typeRuleMap = new HashMap<String, List<AggregationRule>>();
			typeRuleMap.put("FrontEnd", rules);
			ruleMap.put(AggregationManager.PROBLEM_TYPE, typeRuleMap);
			m_handler = new DefaultAggregationHandler();
			m_handler.register(ruleMap);
		}
		

		public List<AggregationRule> getAggregationRule(int type, String domain) {
			List<AggregationRule> rules = new ArrayList<AggregationRule>();
			AggregationRule rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/food");
			rules.add(rule);
			
			for(int i = 0; i < 10000;i ++) {
				rule = new AggregationRule();
				rule.setPattern("http://www.dianping.com/{City}/"+ i);
				rules.add(rule);
			}

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/wedding");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/beauty");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/shopping");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/group");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/car");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/hotel");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/sports");
			rules.add(rule);
			
			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/beauty");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/{City}/other");
			rules.add(rule);
			
			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/review/{reviewid}");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/photos/{photoid}");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/shop/{shopid}");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("{*}/s/j/app/shop/review.{md5:32}.js");
			rules.add(rule);
			
			rule = new AggregationRule();
			rule.setPattern("http://i{x}.dpfile.com/{*}");
			rules.add(rule);

			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/shoplist/{shopListType}");
			rules.add(rule);
			
			rule = new AggregationRule();
			rule.setPattern("http://www.dianping.com/photoList/{photoListType}");
			rules.add(rule);
			
			rule = new AggregationRule();
			rule.setPattern("http://s.dianping.com/{city}/group");
			rules.add(rule);
			return rules;
		}
	}
}
