package com.dianping.cat.report.task.alert;

import com.dianping.cat.Cat;

public class ExceptionBuilder {
	public static void main(String args[]) {
		while (true) {
			Cat.logError(new RuntimeException("just test for FrontEnd"));
			System.out.println("log error");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
