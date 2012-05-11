package com.dianping.cat.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.hadoop.io.Text;
import org.junit.Test;

public class PojoWritableTest {
	private void checkCompareTo(PojoWritable writable) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		writable.write(dos);

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		User user = new User();

		Assert.assertTrue(writable.compareTo(user) != 0);

		user.readFields(dis);

		Assert.assertEquals(0, writable.compareTo(user));
	}

	private void checkRead(PojoWritable expected, String data) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		Text.writeString(dos, data);

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		User user = new User();

		user.readFields(dis);

		Assert.assertEquals(0, expected.compareTo(user));
	}

	private void checkWrite(PojoWritable writable, String expected) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		writable.write(dos);

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		String actual = Text.readString(dis);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testCompareTo() throws IOException {
		checkCompareTo(new User("Alex Bob", 40, true));
		checkCompareTo(new User("Alex", 30, false));
	}

	@Test
	public void testRead() throws IOException {
		checkRead(new User("Alex Bob", 40, true), "Alex Bob|40|true");
		checkRead(new User("Alex", 30, false), "Alex|30|false");
	}

	@Test
	public void testWrite() throws IOException {
		checkWrite(new User("Alex Bob", 40, true), "Alex Bob|40|true");
		checkWrite(new User("Alex", 30, false), "Alex|30|false");
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

		public int getAge() {
			return m_age;
		}

		public String getName() {
			return m_name;
		}

		public boolean isMarried() {
			return m_married;
		}
	}
}
