package com.dianping.cat.consumer.problem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.aggreation.model.entity.AggregationRule;
import com.dianping.cat.consumer.problem.ProblemReportAggregation;
import com.dianping.cat.consumer.problem.aggregation.AggregationConfigManager;
import com.dianping.cat.consumer.problem.aggregation.DefaultAggregationHandler;
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

		aggregation.visitProblemReport(reportOld);

		Assert.assertEquals(reportNew.toString().replaceAll("\r", ""),
		      aggregation.getReport().toString().replaceAll("\r", ""));
	}

	class MockRuleManger extends AggregationConfigManager {
		private void register() {
			List<AggregationRule> rules = getAggregationRule(AggregationConfigManager.PROBLEM_TYPE, Constants.FRONT_END);
			
			m_handler = new DefaultAggregationHandler();
			m_handler.register(rules);
		}

		private AggregationRule buildRule(String pattern){
			AggregationRule rule = new AggregationRule();
			
			rule.setPattern(pattern);
			rule.setType(3);
			rule.setDomain(Constants.FRONT_END);
			return rule;
		}
		
		public List<AggregationRule> getAggregationRule(int type, String domain) {
			List<AggregationRule> rules = new ArrayList<AggregationRule>();
			rules.add(buildRule("http://www.dianping.com/{City}/food"));

			for (int i = 0; i < 1000; i++) {
				rules.add(buildRule("http://www.dianping.com/{City}/" + i));
			}
			rules.add(buildRule("http://www.dianping.com/{City}/wedding"));
			rules.add(buildRule("http://www.dianping.com/{City}/beauty"));
			rules.add(buildRule("http://www.dianping.com/{City}/shopping"));
			rules.add(buildRule("http://www.dianping.com/{City}/group"));
			rules.add(buildRule("http://www.dianping.com/{City}/car"));
			rules.add(buildRule("http://www.dianping.com/{City}/hotel"));
			rules.add(buildRule("http://www.dianping.com/{City}/sports"));
			rules.add(buildRule("http://www.dianping.com/{City}/beauty"));
			rules.add(buildRule("http://www.dianping.com/{City}/other"));
			rules.add(buildRule("http://www.dianping.com/review/{reviewid}"));
			rules.add(buildRule("http://www.dianping.com/photos/{photoid}"));
			rules.add(buildRule("http://www.dianping.com/shop/{shopid}"));
			rules.add(buildRule("{*}/s/j/app/shop/review.{md5:32}.js"));
			rules.add(buildRule("http://i{x}.dpfile.com/{*}"));
			rules.add(buildRule("http://www.dianping.com/shoplist/{shopListType}"));
			rules.add(buildRule("http://www.dianping.com/photoList/{photoListType}"));
			rules.add(buildRule("http://s.dianping.com/{city}/group"));

			return rules;
		}
	}
}
