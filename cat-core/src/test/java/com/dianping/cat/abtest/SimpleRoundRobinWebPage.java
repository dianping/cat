package com.dianping.cat.abtest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.cat.abtest.sample.SampleTest.MyABTestId;

public class SimpleRoundRobinWebPage extends HttpServlet {
	/**
    * 
    */
   private static final long serialVersionUID = -6472784609174835547L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ABTest abtest = ABTestManager.getTest(MyABTestId.CASE1);
		if (abtest.isGroupA()) {
			String a = "This is group A";
			byte[] aByte = a.getBytes();
			response.getOutputStream().write(aByte);
			// Cat.logMetric(...);
		} else if (abtest.isGroupB()) {
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
