package com.dianping.cat.consumer.storage.builder;

import java.util.Arrays;
import java.util.List;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = StorageBuilder.class, value = StorageRPCBuilder.ID)
public class StorageRPCBuilder implements StorageBuilder {

	public final static String ID = "RPC";

	public final static int LONG_THRESHOLD = 100;

	public final static List<String> DEFAULT_METHODS = Arrays.asList("call");

	@Override
	public StorageItem build(Transaction t) {
		String id = null;
		String ip = "default";
		String method = "call";
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("PigeonCall.app")) {
					id = message.getName();
				}

				if (type.equals("PigeonCall.server")) {
					ip = message.getName();
					int index = ip.indexOf(':');

					if (index > -1) {
						ip = ip.substring(0, index);
					}
				}
			}
		}
		return new StorageItem(id, ID, method, ip, LONG_THRESHOLD);
	}

	@Override
	public List<String> getDefaultMethods() {
		return DEFAULT_METHODS;
	}

	@Override
	public String getType() {
		return ID;
	}

	@Override
	public boolean isEligable(Transaction t) {
		String type = t.getType();

		return "PigeonCall".equals(type) || "Call".equals(type);
	}

}
