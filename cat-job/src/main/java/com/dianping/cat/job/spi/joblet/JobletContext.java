package com.dianping.cat.job.spi.joblet;

import java.io.IOException;

public interface JobletContext {
	public void write(Object key, Object value) throws IOException, InterruptedException;
}
