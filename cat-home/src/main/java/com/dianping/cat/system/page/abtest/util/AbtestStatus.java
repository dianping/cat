package com.dianping.cat.system.page.abtest.util;

import java.util.Calendar;
import java.util.Date;

import com.dianping.cat.home.dal.abtest.AbtestRun;


public enum AbtestStatus {

	CREATED, READY, RUNNING, TERMINATED, SUSPENDED;

	private static final int s_deltaTime = -1; // -1 hour

	public static AbtestStatus getByName(String name, AbtestStatus defaultStatus) {
		for (AbtestStatus status : AbtestStatus.values()) {
			if (status.name().equalsIgnoreCase(name)) {
				return status;
			}
		}

		return defaultStatus;
	}


	public String getStatus() {
		return name().toLowerCase();
	}
	
	public static class AbtestStatusUtil {
		public AbtestStatus calculateStatus(AbtestRun run, Date now) {
			Calendar calendar = Calendar.getInstance();
			
			if (run.isDisabled()) {
				return AbtestStatus.SUSPENDED;
			} else {
				Date startDate = run.getStartDate();
				Date endDate = run.getEndDate();

				if (startDate != null) {
					calendar.setTime(startDate);
					calendar.add(Calendar.HOUR, s_deltaTime);
					Date startDelta = calendar.getTime();

					if (now.before(startDelta)) {
						return AbtestStatus.CREATED;
					} else {
						if (now.before(startDate)) {
							return AbtestStatus.READY;
						} else {
							if (endDate != null) {
								if (now.before(endDate)) {
									return AbtestStatus.RUNNING;
								} else {
									return AbtestStatus.TERMINATED;
								}
							} else {
								return AbtestStatus.RUNNING;
							}
						}

					}
				} else {
					if (endDate != null) {
						if (now.before(endDate)) {
							return AbtestStatus.RUNNING;
						} else {
							return AbtestStatus.TERMINATED;
						}
					} else {
						return AbtestStatus.RUNNING;
					}
				}
			}
		}
	}

}
