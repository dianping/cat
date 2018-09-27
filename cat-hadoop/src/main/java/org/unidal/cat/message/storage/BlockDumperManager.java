package org.unidal.cat.message.storage;

public interface BlockDumperManager {
	public void close(int hour);

	public BlockDumper findOrCreate(int hour);
}