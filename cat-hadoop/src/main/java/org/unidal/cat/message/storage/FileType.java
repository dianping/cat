package org.unidal.cat.message.storage;

public enum FileType {
	MAPPING("map"),

	TOKEN("token"),

	INDEX("idx"),

	DATA("dat");

	private String m_extension;

	private FileType(String extension) {
		m_extension = extension;
	}

	public String getExtension() {
		return m_extension;
	}
}