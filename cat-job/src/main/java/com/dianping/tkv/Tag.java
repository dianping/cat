package com.dianping.tkv;

public class Tag implements Comparable<Tag> {
	private int previous = -1;
	private int pos = 0;
	private int next = -1;
	private String name;

	public int getPrevious() {
		return previous;
	}

	public void setPrevious(int previous) {
		this.previous = previous;
	}

	public int getNext() {
		return next;
	}

	public void setNext(int next) {
		this.next = next;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Tag [previous=");
		builder.append(previous);
		builder.append(", pos=");
		builder.append(pos);
		builder.append(", next=");
		builder.append(next);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(Tag o) {
		return this.name.compareTo(o.name);
	}

}