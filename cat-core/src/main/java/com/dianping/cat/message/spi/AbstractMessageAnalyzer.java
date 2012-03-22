package com.dianping.cat.message.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class AbstractMessageAnalyzer<R> implements MessageAnalyzer {
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

		List<R> result = generate();

		store(result);
	}

	public void doCheckpoint() throws IOException {
		// override it
	}

	protected abstract List<R> generate();

	protected List<String> getSortedDomains(Set<String> domains) {
		List<String> sortedDomains = new ArrayList<String>(domains);

		Collections.sort(sortedDomains, new Comparator<String>() {
			@Override
			public int compare(String d1, String d2) {
				if (d1.equals("Cat")) {
					return 1;
				}

				return d1.compareTo(d2);
			}
		});

		return sortedDomains;
	}

	public abstract R getReport(String domain);

	protected abstract boolean isTimeout();

	protected abstract void process(MessageTree tree);

	protected abstract void store(List<R> result);
}
