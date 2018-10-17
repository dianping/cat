/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
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
package com.site.helper;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.helper.Reflects;
import org.unidal.helper.Reflects.IMemberFilter;

public class Stringizers {
	public static JsonStringizer forJson() {
		return JsonStringizer.DEFAULT;
	}

	public static enum JsonStringizer {
		DEFAULT(false),

		COMPACT(true);

		private boolean m_compact;

		private JsonStringizer(boolean compact) {
			m_compact = compact;
		}

		public JsonStringizer compact() {
			return COMPACT;
		}

		public String from(Object obj) {
			return from(obj, 0, 0);
		}

		public String from(Object obj, int maxLength, int maxItemLength) {
			StringBuilder sb = new StringBuilder(1024);
			LengthLimiter limiter = new LengthLimiter(sb, maxLength, maxItemLength);
			Set<Object> done = new HashSet<Object>();

			try {
				fromObject(done, limiter, obj);
			} catch (RuntimeException e) {
				// expected
				sb.append("...");
			}

			return sb.toString();
		}

		private void fromArray(Set<Object> done, LengthLimiter sb, Object obj) {
			int len = Array.getLength(obj);

			sb.append('[');

			for (int i = 0; i < len; i++) {
				if (i > 0) {
					sb.append(',');

					if (!m_compact) {
						sb.append(' ');
					}
				}

				Object element = Array.get(obj, i);

				fromObject(done, sb, element);
			}

			sb.append(']');
		}

		@SuppressWarnings("unchecked")
		private void fromCollection(Set<Object> done, LengthLimiter sb, Object obj) {
			boolean first = true;

			sb.append('[');

			for (Object item : ((Collection<Object>) obj)) {
				if (first) {
					first = false;
				} else {
					sb.append(',');

					if (!m_compact) {
						sb.append(' ');
					}
				}

				fromObject(done, sb, item);
			}

			sb.append(']');
		}

		@SuppressWarnings("unchecked")
		private void fromMap(Set<Object> done, LengthLimiter sb, Object obj) {
			boolean first = true;

			sb.append('{');

			for (Map.Entry<Object, Object> e : ((Map<Object, Object>) obj).entrySet()) {
				Object key = e.getKey();
				Object value = e.getValue();

				if (value == null) {
					continue;
				}

				if (first) {
					first = false;
				} else {
					sb.append(',');

					if (!m_compact) {
						sb.append(' ');
					}
				}

				sb.append('"').append(key).append("\":");

				if (!m_compact) {
					sb.append(' ');
				}

				fromObject(done, sb, value);
			}

			sb.append('}');
		}

		private void fromObject(Set<Object> done, LengthLimiter sb, Object obj) {
			if (obj == null) {
				return;
			}

			Class<?> type = obj.getClass();

			if (type == String.class) {
				sb.append('"').append(obj.toString(), true).append('"');
			} else if (type.isPrimitive() || Number.class.isAssignableFrom(type) || type.isEnum()) {
				sb.append(obj.toString(), true);
			} else if (type == Boolean.class) {
				sb.append(obj.toString(), true);
			} else if (type == Date.class) {
				sb.append('"').append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(obj), true).append('"');
			} else if (type == Class.class) {
				sb.append('"').append(obj, true).append('"');
			} else if (done.contains(obj)) {
				sb.append("{}");
				return;
			} else {
				done.add(obj);

				if (type.isArray()) {
					fromArray(done, sb, obj);
				} else if (Collection.class.isAssignableFrom(type)) {
					fromCollection(done, sb, obj);
				} else if (Map.class.isAssignableFrom(type)) {
					fromMap(done, sb, obj);
				} else {
					fromPojo(done, sb, obj);
				}
			}
		}

		private void fromPojo(Set<Object> done, LengthLimiter sb, Object obj) {
			Class<? extends Object> type = obj.getClass();

			if (hasToString(type)) {
				fromObject(done, sb, obj.toString());
				return;
			}

			List<Method> getters = Reflects.forMethod().getMethods(type, new IMemberFilter<Method>() {
				@Override
				public boolean filter(Method method) {
					return Reflects.forMethod().isGetter(method);
				}
			});

			Collections.sort(getters, new Comparator<Method>() {
				@Override
				public int compare(Method m1, Method m2) {
					return m1.getName().compareTo(m2.getName());
				}
			});

			if (getters.isEmpty()) {
				// use java toString() since we can't handle it
				sb.append(obj.toString());
			} else {
				boolean first = true;

				sb.append('{');

				for (Method getter : getters) {
					String key = Reflects.forMethod().getGetterName(getter);
					Object value;

					try {
						if (!getter.isAccessible()) {
							getter.setAccessible(true);
						}

						value = getter.invoke(obj);
					} catch (Exception e) {
						// ignore it
						value = null;
					}

					if (value == null) {
						continue;
					}

					if (first) {
						first = false;
					} else {
						sb.append(',');

						if (!m_compact) {
							sb.append(' ');
						}
					}

					sb.append('"').append(key).append("\":");

					if (!m_compact) {
						sb.append(' ');
					}

					fromObject(done, sb, value);
				}

				sb.append('}');
			}
		}

		public boolean hasToString(Class<?> type) {
			try {
				Method method = type.getMethod("toString");

				if (method.getDeclaringClass() != Object.class) {
					return true;
				}
			} catch (Exception e) {
				// ignore it
			}

			return false;
		}
	}

	static class LengthLimiter {
		private int m_maxLength;

		private int m_maxItemLength;

		private int m_halfMaxItemLength;

		private StringBuilder m_sb;

		public LengthLimiter(StringBuilder sb, int maxLength, int maxItemLength) {
			m_sb = sb;
			m_maxLength = maxLength - 3;
			m_maxItemLength = maxItemLength;
			m_halfMaxItemLength = maxItemLength / 2 - 1;
		}

		public LengthLimiter append(char ch) {
			m_sb.append(ch);
			return this;
		}

		public LengthLimiter append(Object value) {
			append(value, false);
			return this;
		}

		public LengthLimiter append(Object value, boolean itemLimit) {
			int len = m_sb.length();
			String str = getString(value, itemLimit);

			if (m_maxLength > 0 && len + str.length() > m_maxLength) {
				throw new RuntimeException("Length limited.");
			} else {
				m_sb.append(str);
				return this;
			}
		}

		private String getString(Object value, boolean itemLimit) {
			String str = String.valueOf(value);

			if (itemLimit && m_maxItemLength > 0) {
				int len = str.length();

				if (len > m_maxItemLength) {
					return str.substring(0, m_halfMaxItemLength) + "..." + str.substring(len - m_halfMaxItemLength, len);
				}
			}

			return str;
		}
	}
}
