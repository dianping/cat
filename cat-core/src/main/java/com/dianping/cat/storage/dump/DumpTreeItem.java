package com.dianping.cat.storage.dump;

import org.jboss.netty.buffer.ChannelBuffer;

public class DumpTreeItem {

	private int m_index;
	
	private String m_fileName;
	
	private ChannelBuffer m_buf;

	public int getIndex() {
		return m_index;
	}

	public void setIndex(int index) {
		m_index = index;
	}

	public String getFileName() {
		return m_fileName;
	}

	public DumpTreeItem setFileName(String fileName) {
		m_fileName = fileName;
		return this;
	}

	public ChannelBuffer getBuf() {
		return m_buf;
	}

	public DumpTreeItem setBuf(ChannelBuffer buf) {
		m_buf = buf;
		return this;
	}
}
