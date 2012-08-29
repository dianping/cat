package com.dianping.cat.storage.dump;


public class DumpTreeItem {

	private int m_index;
	
	private String m_fileName;
	
	private int m_size;
	
	private byte[] m_bytes;

	public int getIndex() {
		return m_index;
	}

	public DumpTreeItem setIndex(int index) {
		m_index = index;
		return this;
	}

	public String getFileName() {
		return m_fileName;
	}

	public DumpTreeItem setFileName(String fileName) {
		m_fileName = fileName;
		return this;
	}

	public int getSize() {
		return m_size;
	}

	public DumpTreeItem setSize(int size) {
		m_size = size;
		return this;
	}

	public byte[] getBytes() {
		return m_bytes;
	}

	public void setBytes(byte[] bytes) {
		m_bytes = bytes;
	}
	
}
