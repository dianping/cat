package com.dianping.cat.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class PojoWritableTest {
	@Test
	public void testWrite() throws IOException {
		checkWrite(new User("Alex Bob", 40, true), "Alex Bob|40|true");
		checkWrite(new User("Alex", 30, false), "Alex|30|false");
	}

	@Test
	public void testRead() throws IOException {
		checkRead(new User("Alex Bob", 40, true), "Alex Bob|40|true");
		checkRead(new User("Alex", 30, false), "Alex|30|false");
	}

	private void checkRead(PojoWritable expected, String data) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data.getBytes("utf-8")));
		User user = new User();

		user.readFields(dis);

		Assert.assertEquals(0, expected.compareTo(user));
	}

	private void checkWrite(PojoWritable wirtable, String expected) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		wirtable.write(dos);

		String actual = baos.toString("utf-8");

		Assert.assertEquals(expected, actual);
	}

	public static class User extends PojoWritable {
		private String m_name;

		private int m_age;

		private boolean m_married;

		public User() {
		}

		public User(String name, int age, boolean married) {
			m_name = name;
			m_age = age;
			m_married = married;
		}

		public String getName() {
			return m_name;
		}

		public int getAge() {
			return m_age;
		}

		public boolean isMarried() {
			return m_married;
		}
	}
}
