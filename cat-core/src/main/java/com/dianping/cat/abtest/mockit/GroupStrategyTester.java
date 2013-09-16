package com.dianping.cat.abtest.mockit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.unidal.lookup.ComponentTestCase;
import org.unidal.test.mock.HttpServletRequestMock;
import org.unidal.test.mock.HttpServletResponseMock;
import org.xml.sax.SAXException;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.model.transform.BaseVisitor;
import com.dianping.cat.abtest.model.transform.DefaultSaxParser;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;
import com.dianping.cat.abtest.spi.internal.DefaultABTestContext;

public class GroupStrategyTester extends ComponentTestCase {

	protected int m_id = 1;

	protected String m_url = "abtest.xml";
	
	private ABTestEntity m_entity;
	
	private ABTestGroupStrategy m_groupStrategy;
	
	private Map<String, String> m_cookielets = new LinkedHashMap<String, String>(); 
	
	private boolean isFirst = true;
	
	public void assertGroupStrategy(HttpServletRequest req, HttpServletResponse res,
			String expectedGroup) throws IOException, SAXException,Exception{

		DefaultABTestContext context = initContext();
		
		if (m_entity.isEligible(new Date())) {
			if(isFirst){
				m_groupStrategy.init();
				isFirst = false;
			}
			m_groupStrategy.apply(context);
		}

		Assert.assertEquals(expectedGroup, context.getGroupName());
	}

	private DefaultABTestContext initContext() throws IOException, SAXException {
		ABTestEntity entity = buildEntity();

		DefaultABTestContext context = new DefaultABTestContext(entity);
		context.setCookielets(m_cookielets);
		
		if (!entity.isDisabled()) {
			ABTestGroupStrategy groupStrategy = entity.getGroupStrategy();

			context.setGroupStrategy(groupStrategy);
			m_groupStrategy = groupStrategy;
		}
		
		m_entity = context.getEntity();
		
		return context;
	}

	private ABTestEntity buildEntity() throws IOException, SAXException {
		InputStream in = getClass().getResourceAsStream(m_url);
		AbtestModel abtest = DefaultSaxParser.parse(in);
		ABTestVisitor visitor = new ABTestVisitor();

		abtest.accept(visitor);
		return visitor.getEntity();
	}

	protected void setId(int id) {
		m_id = id;
	}

	protected void setUrl(String url) {
		m_url = url;
	}
	
	protected HttpServletRequestMock mockHttpRequest(){
		return null;
	}
	
	protected HttpServletResponseMock mockHttpResponse(){
		return null;
	}

	private class ABTestVisitor extends BaseVisitor {
		private ABTestEntity m_entity;

		public ABTestEntity getEntity() {
			return m_entity;
		}

		@Override
		public void visitCase(Case _case) {
			for (Run run : _case.getRuns()) {
				if (run.getId() == m_id) {
					m_entity = new ABTestEntity(_case, run);
					try {
						ABTestGroupStrategy strategy = lookup(
								ABTestGroupStrategy.class,
								m_entity.getGroupStrategyName());
						strategy.init();
						m_entity.setGroupStrategy(strategy);

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

}
