package com.dianping.cat.system.alarm.threshold.template;

import com.dianping.cat.home.template.entity.Duration;
import com.dianping.cat.home.template.entity.Param;
import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.home.template.transform.DefaultMerger;

public class ThresholdTemplateMerger extends DefaultMerger {

	public ThresholdTemplateMerger(ThresholdTemplate thresholdTemplate) {
		super(thresholdTemplate);
	}

	@Override
	protected void mergeDuration(Duration old, Duration duration) {
		old.setMin(duration.getMin());
		old.setMax(duration.getMax());
		old.setInterval(duration.getInterval());
		old.setAlarm(duration.getAlarm());
		old.setId(duration.getId());
		old.setAlarmInterval(duration.getAlarmInterval());
	}

	@Override
	protected void mergeParam(Param old, Param param) {
		old.setValue(param.getValue());
		old.setType(param.getType());
	}

}
