package com.dianping.cat.broker;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.Constants;
import com.dianping.cat.config.DefaultUrlPatternHandler;
import com.dianping.cat.config.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.consumer.problem.aggregation.AggregationConfigManager;

public class UrlPatternConvertTest {

	private DefaultUrlPatternHandler m_handler = new DefaultUrlPatternHandler();

	@Test
	public void test() {
		MockRuleManger manager = new MockRuleManger();
		
		manager.register();
		
		Assert.assertEquals("http://www.dianping.com/{City}/food", m_handler.handle("http://www.dianping.com/shanghai/food"));
		Assert.assertEquals("http://www.dianping.com/{City}/beauty", m_handler.handle("http://www.dianping.com/nanjing/beauty"));
		Assert.assertEquals("http://www.dianping.com/{City}/group", m_handler.handle("http://www.dianping.com/nanjing/group"));
		Assert.assertEquals("http://www.dianping.com/wedding", m_handler.handle("http://www.dianping.com/wedding"));
		
	}

	private class MockRuleManger extends UrlPatternConfigManager {
		public void register() {
			List<PatternItem> rules = getPatternItem(AggregationConfigManager.PROBLEM_TYPE, Constants.FRONT_END);

			m_handler = new DefaultUrlPatternHandler();
			m_handler.register(rules);
		}

		private PatternItem buildRule(String pattern) {
			PatternItem item = new PatternItem();

			item.setPattern(pattern);
			return item;
		}

		public List<PatternItem> getPatternItem(int type, String domain) {
			List<PatternItem> rules = new ArrayList<PatternItem>();
			rules.add(buildRule("http://www.dianping.com/{City}/food"));

			for (int i = 0; i < 1000; i++) {
				rules.add(buildRule("http://www.dianping.com/{City}/" + i));
			}
			rules.add(buildRule("http://www.dianping.com/wedding"));
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
