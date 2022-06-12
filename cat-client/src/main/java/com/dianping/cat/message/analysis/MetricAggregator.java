package com.dianping.cat.message.analysis;

import java.util.Map;

public interface MetricAggregator {
   public void aggregate(String name, int count, double total, Map<String, String> tags);
}
