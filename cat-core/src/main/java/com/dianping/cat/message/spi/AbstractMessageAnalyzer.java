package com.dianping.cat.message.spi;

import java.io.IOException;
import java.util.List;

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

	protected abstract boolean isTimeout();

	protected abstract void process(MessageTree tree);

	protected abstract void store(List<R> result);
}
