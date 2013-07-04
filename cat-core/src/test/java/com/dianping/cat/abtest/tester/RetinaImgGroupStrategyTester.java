package com.dianping.cat.abtest.tester;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.unidal.test.mock.HttpServletRequestMock;
import org.xml.sax.SAXException;

import com.dianping.cat.abtest.mockit.GroupStrategyTester;

public class RetinaImgGroupStrategyTester extends GroupStrategyTester {
	
	@Test
	public void test() throws IOException, SAXException, Exception{
		HttpServletRequest req = new MockitABTestRequest();
		
		assertGroupStrategy(req, null, "");
		
	}
	
	
	class MockitABTestRequest extends HttpServletRequestMock {

		public String getParameter(String name) {
			return "202609";
		}

	}


}
