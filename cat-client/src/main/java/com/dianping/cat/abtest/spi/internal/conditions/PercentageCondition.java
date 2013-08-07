package com.dianping.cat.abtest.spi.internal.conditions;

import java.util.Random;

import com.dianping.cat.abtest.model.entity.Condition;

public class PercentageCondition extends AbstractABTestCondition implements ABTestCondition {
	public static final String ID = "percent";
	
	private int m_percent = -1;
	
	private Random m_random = new Random();

	@Override
	public boolean accept(Condition condition) {
		if(m_percent == -1){
			m_percent = Integer.parseInt(condition.getText());
		}
		
		int random = m_random.nextInt(100) + 1;
		
		if(random <= m_percent){
			return true;
		}else{
			return false;
		}
	}
}
