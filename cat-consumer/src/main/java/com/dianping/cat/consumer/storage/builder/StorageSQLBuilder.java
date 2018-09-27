package com.dianping.cat.consumer.storage.builder;

import java.util.Arrays;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.DatabaseParser.Database;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = StorageBuilder.class, value = StorageSQLBuilder.ID)
public class StorageSQLBuilder implements StorageBuilder {

	@Inject
	private DatabaseParser m_databaseParser;

	public final static String ID = "SQL";

	public final static int LONG_THRESHOLD = 1000;

	public final static List<String> DEFAULT_METHODS = Arrays.asList("select", "delete", "insert", "update");

	@Override
	public StorageItem build(Transaction t) {
		String id = null;
		String method = "select";
		String ip = "default";
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Method")) {
					method = message.getName().toLowerCase();
				}
				if (type.equals("SQL.Database")) {
					Database database = m_databaseParser.parseDatabase(message.getName());

					if (database != null) {
						ip = database.getIp();
						id = database.getName();
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
		return "SQL".equals(t.getType());
	}

}
