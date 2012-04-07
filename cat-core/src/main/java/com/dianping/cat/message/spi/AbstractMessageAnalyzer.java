package com.dianping.cat.message.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.site.lookup.ContainerHolder;

public abstract class AbstractMessageAnalyzer<R> extends ContainerHolder implements MessageAnalyzer {
	@Override
	public void analyze(MessageQueue queue) {
		while (!isTimeout()) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				process(tree);
			}
		}

		while (true) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				process(tree);
			} else {
				break;
			}
		}
	}

	protected boolean isLocalMode() {
		ServerConfigManager manager = lookup(ServerConfigManager.class);
		ServerConfig config = manager.getServerConfig();

		// local mode should be turned off explicitly
		if (config != null && !config.isLocalMode()) {
			return false;
		} else {
			return true;
		}
	}

	public void doCheckpoint(boolean atEnd) {
		// override it
	}

	protected List<String> getSortedDomains(Set<String> domains) {
		List<String> sortedDomains = new ArrayList<String>(domains);

		Collections.sort(sortedDomains, new Comparator<String>() {
			@Override
			public int compare(String d1, String d2) {
				if (d1.equals("Cat")) {
					return 1;
				} else if (d2.equals("Cat")) {
					return -1;
				}

				return d1.compareTo(d2);
			}
		});

		return sortedDomains;
	}

	public abstract R getReport(String domain);

	protected abstract boolean isTimeout();

	protected abstract void process(MessageTree tree);
}
