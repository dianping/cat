package com.dianping.cat.message.storage;

import java.util.ArrayList;
import java.util.List;

public class MessageBlock {
	private String m_dataFile;

	private byte[] m_data;

	private List<Integer> m_indexes = new ArrayList<Integer>(32);

	private List<Integer> m_sizes = new ArrayList<Integer>(32);

	public MessageBlock(String dataFile) {
		m_dataFile = dataFile;
	}

	public void addIndex(int index, int size) {
		m_indexes.add(index);
		m_sizes.add(size);
	}

	public int getBlockSize() {
		return m_indexes.size();
	}

	public byte[] getData() {
		return m_data;
	}

	public String getDataFile() {
		return m_dataFile;
	}

	public int getIndex(int index) {
		return m_indexes.get(index);
	}

	public int getSize(int index) {
		return m_sizes.get(index);
	}

	public void setData(byte[] data) {
		m_data = data;
	}
}
