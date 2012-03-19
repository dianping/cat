package com.dianping.cat.storage.mysql;

public class LogView {

	private long offset;

	private int length;

	private String path;

	private String tagThread;

	private String tagSession;

	private String tagRequest;

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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTagThread() {
		return tagThread;
	}

	public void setTagThread(String tagThread) {
		this.tagThread = tagThread;
	}

	public String getTagSession() {
		return tagSession;
	}

	public void setTagSession(String tagSession) {
		this.tagSession = tagSession;
	}

	public String getTagRequest() {
		return tagRequest;
	}

	public void setTagRequest(String tagRequest) {
		this.tagRequest = tagRequest;
	}

}
