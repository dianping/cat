package org.unidal.cat.message.storage.exception;

public class BlockQueueFullException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BlockQueueFullException(String message) {
		super(message);
	}
}
