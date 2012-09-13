/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-12
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.engine.helper;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SQLWildcardTest {
	@Test
	public void testLikes() {
		checkLike("abc", "%", true);
		checkLike("abc", "a_c", true);
		checkLike("abc", "a%", true);
		checkLike("abc", "%c", true);
		checkLike("abc", "a%c", true);
		checkLike("abc", "_bc", true);
		checkLike("abc", "ab_", true);
		checkLike("abc", "ac", false);
		checkLike("abc", "abcabc", false);
		checkLike("abc", "", false);
	}

	private void checkLike(String str, String pattern, boolean expected) {
		boolean actual = SQLRegex.like(str, pattern);
		Assert.assertEquals(expected, actual);
	}
}
