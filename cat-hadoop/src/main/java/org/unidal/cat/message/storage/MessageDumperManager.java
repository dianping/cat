package org.unidal.cat.message.storage;

public interface MessageDumperManager {
	public abstract void close(int hour);

	public abstract MessageDumper find(int hour);

	public abstract MessageDumper findOrCreate(int hour);
}