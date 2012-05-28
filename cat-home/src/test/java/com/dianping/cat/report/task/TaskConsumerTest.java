package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.hadoop.dal.Task;

public class TaskConsumerTest {

	public static class TaskConsumerWrap extends TaskConsumer {

		final List<Integer> replayer = new ArrayList<Integer>();

		@Override
		protected Task findDoingTask() {
			replayer.add(1);
			return null;
		}

		@Override
		protected void updateDoingToDone(Task doing) {
			replayer.add(3);
		}

		@Override
		protected void taskNotFindDuration() {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			replayer.add(4);
			this.stop();
		}

		@Override
		protected void mergeYesterdayReport() {
			replayer.add(6);
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
		protected void taskFailDuration() {
			replayer.add(11);
		}

		@Override
		protected void failTask(Task todo) {
			replayer.add(5);
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
			protected Task findDoingTask() {
				super.findDoingTask();
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

			@Override
			protected boolean processTask(Task doing) {
				super.processTask(doing);
				return true;
			}

			@Override
			protected void updateDoingToDone(Task doing) {
				super.updateDoingToDone(doing);
				doing.setStatus(STATUS_DONE);
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(100);
		}

		Assert.assertEquals("[1, 10, 3, 6, 1, 8, 4]", Arrays.toString(consumer.replayer.toArray()));

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
			protected Task findDoingTask() {
				super.findDoingTask();
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(100);
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
	public void testToDoTaskSuccess() throws InterruptedException {
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
			protected void updateDoingToDone(Task doing) {
				super.updateDoingToDone(doing);
				t.setStatus(TaskConsumer.STATUS_DONE);
			}

			@Override
			protected Task findTodoTask() {
				super.findTodoTask();
				return taskList.size() == 0 ? null : taskList.remove(0);
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(100);
		}

		Assert.assertEquals("[1, 8, 7, 10, 3, 6, 1, 8, 4]", Arrays.toString(consumer.replayer.toArray()));
		Assert.assertEquals(TaskConsumer.STATUS_DONE, t.getStatus());
	}

	/**
	 * todo task, 执行失败
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testToDoTaskFail() throws InterruptedException {
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
			protected void failTask(Task todo) {
				super.failTask(todo);
				todo.setStatus(STATUS_FAIL);
			}

		};

		new Thread(consumer).start();
		while (!consumer.isStopped()) {
			Thread.sleep(100);
		}

		Assert.assertEquals("[1, 8, 7, 10, 11, 10, 11, 10, 5, 1, 8, 4]", Arrays.toString(consumer.replayer.toArray()));
		Assert.assertEquals(TaskConsumer.STATUS_FAIL, t.getStatus());
	}
}
