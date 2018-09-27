package org.unidal.cat.message.storage.exception;

public class MessageQueueFullException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MessageQueueFullException(String message) {
		super(message);
	}
}
