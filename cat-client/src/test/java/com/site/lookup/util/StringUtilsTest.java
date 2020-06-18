/*
 * Copyright (c) 2011-2019, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.site.lookup.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertFalse(StringUtils.isEmpty("foo"));
    }

    @Test
    public void testIsNotEmpty() {
        Assert.assertFalse(StringUtils.isNotEmpty(""));
        Assert.assertFalse(StringUtils.isNotEmpty(null));
        Assert.assertTrue(StringUtils.isNotEmpty("foo"));
    }

    @Test
    public void testJoinArray() {
        Assert.assertEquals("", StringUtils
                .join(new String[0], ","));
        Assert.assertEquals("foo", StringUtils
                .join(new String[]{"foo"}, ","));
        Assert.assertEquals("foo,bar,baz", StringUtils
                .join(new String[]{"foo", "bar", "baz"}, ","));
    }

    @Test
    public void testJoinCollection() {
        Assert.assertEquals("", StringUtils
                .join(new ArrayList<String>(), ","));
        Assert.assertEquals("foo", StringUtils
                .join(new ArrayList<>(Arrays.asList("foo")), ","));
        Assert.assertEquals("foo,bar,baz", StringUtils
                .join(new ArrayList<>(
                        Arrays.asList("foo", "bar", "baz")), ","));
    }

    @Test
    public void testNormalizeSpace() {
        Assert.assertEquals(" f o o b a r",
                StringUtils.normalizeSpace(" f\to\ro\nb a r "));
    }

    @Test
    public void testTrimAll() {
        Assert.assertNull(StringUtils.trimAll(null));
        Assert.assertEquals("foobar", StringUtils.trimAll(" f\to\ro\nb a r "));
    }
}
