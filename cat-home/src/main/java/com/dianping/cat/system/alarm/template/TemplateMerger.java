package com.dianping.cat.system.alarm.template;

import com.dianping.cat.home.template.entity.Connection;
import com.dianping.cat.home.template.entity.Duration;
import com.dianping.cat.home.template.entity.Param;
import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.home.template.transform.DefaultMerger;

public class TemplateMerger extends DefaultMerger{

	public TemplateMerger(ThresholdTemplate thresholdTemplate) {
	   super(thresholdTemplate);
   }

	@Override
   protected void mergeConnection(Connection old, Connection connection) {
	   super.mergeConnection(old, connection);
   }

	@Override
   protected void mergeDuration(Duration old, Duration duration) {
	   old.setMin(duration.getMin());
	   old.setMax(duration.getMax());
	   old.setInterval(duration.getInterval());
	   old.setAlarm(duration.getAlarm());
	   old.setId(duration.getId());
   }

	@Override
   protected void mergeParam(Param old, Param param) {
		old.setValue(param.getValue());
		old.setType(param.getType());
   }


	@Override
   protected void mergeThresholdTemplate(ThresholdTemplate old, ThresholdTemplate thresholdTemplate) {
	   super.mergeThresholdTemplate(old, thresholdTemplate);
   }

}
