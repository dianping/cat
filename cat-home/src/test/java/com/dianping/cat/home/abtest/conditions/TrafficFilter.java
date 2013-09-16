package com.dianping.cat.home.abtest.conditions;

import java.util.Random;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.abtest.spi.internal.conditions.ABTestCondition;

public class TrafficFilter {
	private Condition1 m_condition1 = new Condition1();

	private Condition2 m_condition2 = new Condition2();

	private Condition3 m_condition3 = new Condition3();

	private Condition4 m_condition4 = new Condition4();

	private Condition5 m_condition5 = new Condition5();

	public boolean isEligible(HttpServletRequest request) {
		if (m_condition1.accept(request) || m_condition2.accept(request) && m_condition3.accept(request)
		      && (m_condition4.accept(request)) && m_condition5.accept(request)) {
			return true;
		}
		return false;
	}

	public class Condition1 implements ABTestCondition {
		@Override
		public boolean accept(HttpServletRequest request) {
			String actual = request.getRequestURL().toString();
			if (actual.equalsIgnoreCase("http://www.dianping.com")) {
				return true;
			} else {
				return false;
			}
		}
	}

	public class Condition2 implements ABTestCondition {
		@Override
		public boolean accept(HttpServletRequest request) {
			String actual = request.getRequestURL().toString();
			if (actual.toLowerCase().startsWith("http://www.dianping.com/".toLowerCase())) {
				return true;
			} else {
				return false;
			}
		}
	}

	public class Condition3 implements ABTestCondition {
		@Override
		public boolean accept(HttpServletRequest request) {
			String actual = request.getRequestURL().toString();
			if (!actual.equalsIgnoreCase("http://www.dianping.com1")) {
				return true;
			} else {
				return false;
			}
		}
	}

	public class Condition4 implements ABTestCondition {
		@Override
		public boolean accept(HttpServletRequest request) {
			String actual = request.getRequestURL().toString();
			if (!actual.equalsIgnoreCase("http://www.dianping.com/1")) {
				return true;
			} else {
				return false;
			}
		}
	}

	public class Condition5 implements ABTestCondition {
		private int m_percent = -1;

		private Random m_random = new Random();

		@Override
		public boolean accept(HttpServletRequest request) {
			if (m_percent == -1) {
				m_percent = 100;
			}
			if (m_percent == 100) {
				return true;
			}
			int random = m_random.nextInt(100) + 1;
			if (random <= m_percent) {
				return true;
			} else {
				return false;
			}
		}
	}
}