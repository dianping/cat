package com.dianping.abtest.sample;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestManager;
import com.dianping.cat.abtest.ABTestName;

public class ABTestSampleServlet extends HttpServlet {
	private static final long serialVersionUID = -6472784609174835547L;

	private ABTest m_abtest = ABTestManager.getTest(MyABTestId.CASE1);

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Cat.logError(new Exception());
		
		if (m_abtest.isGroupA()) {
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/index1.jsp");
			Cat.logMetric("ABTestSample", "page", "index1", "group", "a");
			rd.forward(request, response);
		} else {
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/index2.jsp");
			Cat.logMetric("ABTestSample", "page", "index2", "group", "");
			rd.forward(request, response);
		}
	}

	public static enum MyABTestId implements ABTestName {
		CASE1("TestConditions");

		private String m_id;

		private MyABTestId(String id) {
			m_id = id;
		}

		@Override
		public String getValue() {
			return m_id;
		}
	}
}
