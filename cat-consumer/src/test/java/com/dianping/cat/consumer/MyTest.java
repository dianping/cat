package com.dianping.cat.consumer;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MyTest {
	public static void main(String[] args) {
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");

		// second comment
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");
		System.out.println("second");

	}

	@Test
	public void firstCase() {
		Assert.assertEquals("First", "Fir" + "st");
	}

	@Test
	@Ignore("need database connection")
	public void secondCase() {
		System.out.println("Second");
	}
}
