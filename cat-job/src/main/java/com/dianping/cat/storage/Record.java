package com.dianping.cat.storage;

public class Record {

	public static final char TAG_SPLITER = '\t';

	public static final byte ENDER = '\n';

	private int pos;

	private String key;

	private byte[] value;

	private String[] tags;

	private int tagsLength;

	private String tagsString;

	private String nextKey;

	private String priviousKey;

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public int getBodyLength() {
		return 12 + key.length() + value.length + tagsLength;
	}

	public int getTagsLength() {
		return tagsLength;
	}

	public void setTagsLength(int tagsLength) {
		this.tagsLength = tagsLength;
	}

	public String getTagsToString() {
		return this.tagsString;
	}

	public void setTagsString(String string) {
		this.tagsString = string;
	}

	public String nextKey() {
		return this.nextKey;
	}

	public String priviousKey() {
		return this.priviousKey;
	}

	public void setPriviousKey(String priviousKey) {
		this.priviousKey = priviousKey;
	}

	public void setNexKey(String nextKey) {
		this.nextKey = nextKey;
	}
	
}
