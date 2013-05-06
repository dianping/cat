package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.dainping.cat.consumer.core.dal.Task;
import com.dianping.cat.report.task.thread.TaskConsumer;

public class TaskConsumerTest {

	public static class TaskConsumerWrap extends TaskConsumer {

		final List<Integer> replayer = new ArrayList<Integer>();

		@Override
		protected Task findDoingTask(String ip) {
			replayer.add(1);
			return null;
		}

		@Override
		protected boolean updateDoingToDone(Task doing) {
			replayer.add(3);
			return false;
		}

		@Override
		protected void taskNotFoundDuration() {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}
			replayer.add(4);
			this.stop();
		}

		@Override
		protected boolean updateTodoToDoing(Task todo) {
			replayer.add(7);
			return false;
		}

		@Override
		protected Task findTodoTask() {
			replayer.add(8);
			return null;
		}

		@Override
		protected boolean processTask(Task doing) {
			replayer.add(10);
			return false;
		}

		@Override
		protected void taskRetryDuration() {
			replayer.add(11);
		}

		@Override
		protected boolean updateDoingToFailure(Task todo) {
			replayer.add(5);
			return false;
		}

		@Override
		protected long getSleepTime() {
			return 100;
		}

		@Override
      public String getName() {
	      return "MockConsumer";
      }

		@Override
      public void shutdown() {
      }
	};

	/**
	 * 发现已经存在的doing task,继续执行成功
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testContinueDoingTaskSuccess() throws InterruptedException {
		final Task t = new Task();
		t.setStatus(TaskConsumer.STATUS_DOING);

		final List<Task> taskList = new ArrayList<Task>();
		taskList.add(t);

		TaskConsumerWrap consumer = new TaskConsumerWrap() {
			@Override
			protected Task findDoingTask(String ip) {
				super.findDoingTask(ip);
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

			@Override
			protected boolean processTask(Task doing) {
				super.processTask(doing);
				return true;
			}

			@Override
			protected boolean updateDoingToDone(Task doing) {
				super.updateDoingToDone(doing);
				doing.setStatus(STATUS_DONE);
				return true;
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(10);
		}

		String expectValue = Arrays.toString(consumer.replayer.toArray());

		Assert.assertEquals("[1, 10, 3, 1, 8, 4]", expectValue);

		Assert.assertEquals(TaskConsumer.STATUS_DONE, t.getStatus());

	}

	/**
	 * 发现已经存在的doing task,继续执行失败
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testContinueDoingTaskFail() throws InterruptedException {
		final Task t = new Task();
		t.setStatus(TaskConsumer.STATUS_DOING);

		final List<Task> taskList = new ArrayList<Task>();
		taskList.add(t);

		TaskConsumerWrap consumer = new TaskConsumerWrap() {
			@Override
			protected Task findDoingTask(String ip) {
				super.findDoingTask(ip);
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(10);
		}

		Assert.assertEquals("[1, 10, 11, 10, 11, 10, 5, 1, 8, 4]", Arrays.toString(consumer.replayer.toArray()));
		Assert.assertEquals(TaskConsumer.STATUS_DOING, t.getStatus());
	}

	/**
	 * 发现todo task, 执行成功
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testTodoTaskSuccess() throws InterruptedException {
		final Task t = new Task();
		t.setStatus(TaskConsumer.STATUS_TODO);

		final List<Task> taskList = new ArrayList<Task>();
		taskList.add(t);

		final TaskConsumerWrap consumer = new TaskConsumerWrap() {

			@Override
			protected boolean updateTodoToDoing(Task todo) {
				super.updateTodoToDoing(todo);
				return true;
			}

			@Override
			protected boolean processTask(Task doing) {
				super.processTask(doing);
				return true;
			}

			@Override
			protected boolean updateDoingToDone(Task doing) {
				super.updateDoingToDone(doing);
				t.setStatus(TaskConsumer.STATUS_DONE);
				return true;
			}

			@Override
			protected Task findTodoTask() {
				super.findTodoTask();
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(10);
		}

		Assert.assertEquals("[1, 8, 7, 10, 3, 1, 8, 4]", Arrays.toString(consumer.replayer.toArray()));
		Assert.assertEquals(TaskConsumer.STATUS_DONE, t.getStatus());
	}

	/**
	 * todo task, 执行失败
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testTodoTaskFail() throws InterruptedException {
		final Task t = new Task();
		t.setStatus(TaskConsumer.STATUS_TODO);

		final List<Task> taskList = new ArrayList<Task>();
		taskList.add(t);

		final TaskConsumerWrap consumer = new TaskConsumerWrap() {

			@Override
			protected boolean updateTodoToDoing(Task todo) {
				super.updateTodoToDoing(todo);
				return true;
			}

			@Override
			protected Task findTodoTask() {
				super.findTodoTask();
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

			@Override
			protected boolean updateDoingToFailure(Task todo) {
				super.updateDoingToFailure(todo);
				todo.setStatus(STATUS_FAIL);
				return true;
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(10);
		}

		Assert.assertEquals("[1, 8, 7, 10, 11, 10, 11, 10, 5, 1, 8, 4]", Arrays.toString(consumer.replayer.toArray()));
		Assert.assertEquals(TaskConsumer.STATUS_FAIL, t.getStatus());
	}

	public boolean possibleResult(String actual) {
		// 当抢占到同一TODO task时
		final String expect1 = "[1, 8, 1, 8, 4]";
		// 当未抢占到同一TODO task时
		final String expect2 = "[1, 8, 4]";

		return actual.equals(expect1) || actual.equals(expect2);
	}
}
