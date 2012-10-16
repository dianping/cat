package com.dianping.cat.system.alarm.service;

import java.util.List;

import com.dianping.cat.system.alarm.template.ThresholdRule;
import com.site.helper.Threads.Task;

public class ServiceRuleManager {

	public List<ThresholdRule> getAllServiceRules(){
		return null;
	}
	
	public static class ServiceRuleRefresh implements Task{

		@Override
      public void run() {
      }

		@Override
      public String getName() {
	      return null;
      }

		@Override
      public void shutdown() {
      }
	}
	
}
