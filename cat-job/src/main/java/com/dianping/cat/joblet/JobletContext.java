package com.dianping.cat.joblet;

import java.io.IOException;

public interface JobletContext {
	public void write(Object key, Object value) throws IOException, InterruptedException;
}
