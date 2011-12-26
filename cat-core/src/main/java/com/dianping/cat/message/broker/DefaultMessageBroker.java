package com.dianping.cat.message.broker;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.handler.MessageHandler;
import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.io.MessageSender;
import com.site.lookup.annotation.Inject;

public class DefaultMessageBroker implements MessageBroker {
	@Inject
	private MessageReceiver m_reciever;

	@Inject
	private MessageSender m_sender;

	@Override
	public void run() {
		m_reciever.onMessage(new MessageHandler() {
			@Override
			public void handle(Message message) {
				m_sender.send(message);
			}
		});
	}

	public void setReciever(MessageReceiver reciever) {
		m_reciever = reciever;
	}

	public void setSender(MessageSender sender) {
		m_sender = sender;
	}
}
