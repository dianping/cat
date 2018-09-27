package com.dianping.cat.analysis;

import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;

public interface MessageAnalyzer {

	public boolean isEligable(MessageTree tree);
	
	public void analyze(MessageQueue queue);

	public void destroy();

	public void doCheckpoint(boolean atEnd);

	public long getStartTime();

	public void initialize(long startTime, long duration, long extraTime);

	public int getAnanlyzerCount(String name);

	public void setIndex(int index);

	public ReportManager<?> getReportManager();
}
