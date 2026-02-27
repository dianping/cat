package com.dianping.cat.configuration;

public interface ConfigureProperty {
	String BLOCKED = "blocked";

	String DUMP_LOCKED = "dumpLocked";

	String TREE_LENGTH_LIMIT = "cat.tree.max.length";

	String TAGGED_TRANSACTION_CACHE_SIZE = "tagged.transadtion.cache.size";

	String SENDER_MESSAGE_QUEUE_SIZE = "cat.queue.length";

	String ROUTERS = "routers";

	String SAMEPLE_RATIO = "sample.ratio";

	String START_TRANSACTION_TYPES = "startTransactionTypes";

	String MATCH_TRANSACTION_TYPES = "matchTransactionTypes";

	String MAX_MESSAGE_LENGTH = "max-message-lines";

	String NETWORK_WORKER_THREADS = "network.worker.threads";

	String EPOLL_ENABLED = "network.epoll.enabled";

	String RECONNECT_INTERVAL = "network.reconnect.interval";
}
