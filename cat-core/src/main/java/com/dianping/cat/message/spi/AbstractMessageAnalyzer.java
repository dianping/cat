package com.dianping.cat.message.spi;

public abstract class AbstractMessageAnalyzer<R> implements MessageAnalyzer {
	@Override
	public void analyze(MessageQueue queue) {
		while (!isTimeout()) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				process(tree);
			}
		}

		// 已经过期了，但是任务队列还有任务处理。
		while (true) {
			MessageTree tree = queue.poll();

			if (tree != null) {
				process(tree);
			} else {
				break;
			}
		}

		R result = generate();

		store(result);
	}

	protected abstract void store(R result);

	public abstract R generate();

	protected abstract void process(MessageTree tree);

	protected abstract boolean isTimeout();
}
