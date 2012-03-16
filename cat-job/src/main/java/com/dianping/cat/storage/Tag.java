package com.dianping.cat.storage;

public class Tag  {
	private int previous = -1;
	private int pos = 0;
	private int next = -1;
	private String name;

	public String getName() {
		return name;
	}

	public int getNext() {
		return this.next;
	}

	public int getPos() {
		return pos;
	}

	public int getPrevious() {
		return previous;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public void setPrevious(int previous) {
		this.previous = previous;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tag [previous=");
		builder.append(previous);
		builder.append(", next=");
		builder.append(next);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

}