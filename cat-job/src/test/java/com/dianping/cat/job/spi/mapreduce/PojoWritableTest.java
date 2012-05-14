package com.dianping.cat.job.spi.mapreduce;

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

	@Test
	public void testFieldMeta() {
		MockPojo p1 = new MockPojo();
		MockPojo p2 = new MockPojo();

		p1.setBoolean(false);
		p1.setDouble(12.34);
		p1.setInteger(4);
		p1.setString("567");

		p2.setBoolean(true);
		p2.setDouble(123.4);
		p2.setInteger(4);
		p2.setString("567");

		Assert.assertEquals(0, p1.compareTo(p2));
		
		Assert.assertEquals("false|4|567|12.34", p1.toString());
		Assert.assertEquals("true|4|567|123.4", p2.toString());
	}

	public static class MockPojo extends PojoWritable {
		private String m_string;

		@FieldMeta(order = 2)
		private int m_integer;

		@FieldMeta(key = false, order = 0)
		private boolean m_boolean;

		@FieldMeta(key = false)
		private double m_double;

		public MockPojo() {
		}

		public String getString() {
			return m_string;
		}

		public void setString(String string) {
			m_string = string;
		}

		public int getInteger() {
			return m_integer;
		}

		public void setInteger(int integer) {
			m_integer = integer;
		}

		public boolean isBoolean() {
			return m_boolean;
		}

		public void setBoolean(boolean b) {
			m_boolean = b;
		}

		public double getDouble() {
			return m_double;
		}

		public void setDouble(double d) {
			m_double = d;
		}
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
