package com.dianping.dog.alarm.rule;

import java.util.Comparator;
import java.util.TreeSet;

import com.dianping.dog.alarm.problem.ProblemEvent;

public class PeriodStorage implements Storage<ProblemEvent, Integer> {
	
	private long m_period;

	private TreeSet<ProblemEvent> sortedDatas;

	@Override
	public void init(long period) {
		m_period = period;
		sortedDatas = new TreeSet<ProblemEvent>();
	}

	@Override
	public void save(ProblemEvent data, long timeStamp) {
		WrapperProblemEvent wrapper = new WrapperProblemEvent(data);
       long expiredTime = timeStamp - m_period;
       //sortedDatas.subSet(wrapper, toElement);
	}

	@Override
	public Integer getData(DataVistor<ProblemEvent, Integer> vistor) {
		return null;
	}

}


class WrapperProblemEvent implements Comparator<ProblemEvent>{
	
	private ProblemEvent m_problemEvent;
	
	public WrapperProblemEvent(ProblemEvent event){
		m_problemEvent = event;
	}

	public ProblemEvent getProblemEvent(){
		return m_problemEvent;
	}

	@Override
   public int compare(ProblemEvent o1, ProblemEvent o2) {
		long t1 = o1.getTimestamp().getTime();
		long t2 = o2.getTimestamp().getTime();
	   return (int) (t1-t2);
   }
	
}