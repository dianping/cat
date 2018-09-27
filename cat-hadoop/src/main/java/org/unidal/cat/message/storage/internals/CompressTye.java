package org.unidal.cat.message.storage.internals;

public enum CompressTye {

	GZIP("gzip"),

	DEFLATE("deflate"),

	SNAPPY("snappy");

	private String m_name;

	private CompressTye(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public static CompressTye getCompressTye(String name) {
		for (CompressTye type : values()) {
			if (name.equals(type.getName())) {
				return type;
			}
		}
		return GZIP;
	}

}
