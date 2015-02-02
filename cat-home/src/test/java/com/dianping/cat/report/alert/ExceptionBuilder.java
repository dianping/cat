package com.dianping.cat.report.alert;

import com.dianping.cat.Cat;

public class ExceptionBuilder {
	public static void main(String[] args) {
		while (true) {
			Cat.logEvent("Error", "http://www.dianping.com/shop/{shopid}","ERROR", null);
			try {
				Thread.sleep(50000);
				System.out.println("log error");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
