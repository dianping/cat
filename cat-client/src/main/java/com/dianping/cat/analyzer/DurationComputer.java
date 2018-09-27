package com.dianping.cat.analyzer;

public class DurationComputer {

	public static int computeDuration(int duration) {
		if (duration < 1) {
			return 1;
		} else if (duration < 20) {
			return duration;
		} else if (duration < 200) {
			return duration - duration % 5;
		} else if (duration < 500) {
			return duration - duration % 20;
		} else if (duration < 2000) {
			return duration - duration % 50;
		} else if (duration < 20000) {
			return duration - duration % 500;
		} else if (duration < 1000000) {
			return duration - duration % 10000;
		} else {
			int dk = 524288;

			if (duration > 3600 * 1000) {
				dk = 3600 * 1000;
			} else {
				while (dk < duration) {
					dk <<= 1;
				}
			}
			return dk;
		}
	}

}
