package com.dianping.cat.consumer.storage.builder;

import java.util.Arrays;
import java.util.List;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = StorageBuilder.class, value = StorageCacheBuilder.ID)
public class StorageCacheBuilder implements StorageBuilder {

	public final static String ID = "Cache";

	public final static int LONG_THRESHOLD = 50;

	public final static List<String> DEFAULT_METHODS = Arrays.asList("add", "get", "mGet", "remove");

	@Override
	public StorageItem build(Transaction t) {
		String ip = "default";
		String id = t.getType();
		int index = id.indexOf(".");

		if (index > -1) {
			id = id.substring(index + 1);
		}
		String name = t.getName();
		String method = name.substring(name.lastIndexOf(":") + 1);
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.endsWith(".server")) {
					ip = message.getName();
					index = ip.indexOf(":");

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

		return type != null && (type.startsWith("Cache.") || type.startsWith("Squirrel."));
	}

}
