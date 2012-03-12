package com.dianping.tkv;

import java.util.HashMap;
import java.util.Map;

public class Meta {
	private String key;

	private long offset;

	private int length;

	private Map<String, Tag> tags;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public Map<String, Tag> getTags() {
		return tags;
	}

	public void addTag(String tagName) {
		if (tags == null) {
			tags = new HashMap<String, Tag>();
		}
		Tag t = new Tag();
		t.setName(tagName);
		tags.put(tagName, t);
	}

	public void addTag(Tag tag) {
		if (tags == null) {
			tags = new HashMap<String, Tag>();
		}
		tags.put(tag.getName(), tag);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Meta [key=");
		builder.append(key);
		builder.append(", offset=");
		builder.append(offset);
		builder.append(", length=");
		builder.append(length);
		builder.append(", tags=");
		builder.append(tags);
		builder.append("]");
		return builder.toString();
	}

}
