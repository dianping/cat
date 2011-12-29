package com.dianping.cat.message.broker;

import com.dianping.cat.message.io.MessageReceiver;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.MessageTree;
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
			public void handle(MessageTree tree) {
				m_sender.send(tree);
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
