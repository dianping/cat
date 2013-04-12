package com.dianping.cat.abtest.demo.roundrobin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestManager;
import com.dianping.cat.abtest.sample.SampleTest.MyABTestId;

public class SimpleRoundRobinServlet extends HttpServlet {
	private static final long serialVersionUID = -6472784609174835547L;

	private ABTest m_abtest = ABTestManager.getTest(MyABTestId.CASE1);

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (m_abtest.isGroupA()) {
			String a = "This is group A";
			byte[] aByte = a.getBytes();
			response.getOutputStream().write(aByte);
			// Cat.logMetric(...);
		} else if (m_abtest.isGroupB()) {
			String b = "This is group B";
			byte[] bByte = b.getBytes();
			response.getOutputStream().write(bByte);
			// Cat.logMetric(...);
		} else {
			String b = "This is group dfault";
			byte[] bByte = b.getBytes();
			response.getOutputStream().write(bByte);
			// Cat.logMetric(...);
		}
	}
}
