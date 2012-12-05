package com.dianping.cat.job.spi.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public abstract class PojoWritable implements WritableComparable<PojoWritable> {
	private static Map<Class<?>, Entry> s_entries = new HashMap<Class<?>, Entry>();

	@Override
	public int compareTo(PojoWritable o) {
		Entry entry = getEntry();

		try {
			for (Field field : entry.getFields()) {
				if (!entry.isKey(field)) {
					continue;
				}

				Object v1 = field.get(this);
				Object v2 = field.get(o);

				if (v1 == null && v2 == null) {
					continue;
				} else if (v1 == null && v2 != null) {
					return -1;
				} else if (v1 != null && v2 == null) {
					return 1;
				} else if (v1 instanceof Comparable && v2 instanceof Comparable) {
					@SuppressWarnings("unchecked")
					int result = ((Comparable<Object>) v1).compareTo((Comparable<Object>) v2);

					if (result != 0) {
						return result;
					}
				} else {
					throw new RuntimeException(String.format("Unable to compare object(%s) and object(%s)!", this, o));
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("Error when getting field value from object(%s) or object(%s)", this,
			      o), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(String.format("Error when getting field value from object(%s) or object(%s)", this,
			      o), e);
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PojoWritable) {
			return compareTo((PojoWritable) obj) == 0;
		} else {
			return false;
		}
	}

	private Entry getEntry() {
		Class<?> clazz = getClass();
		Entry entry = s_entries.get(clazz);

		if (entry == null) {
			synchronized (s_entries) {
				entry = s_entries.get(clazz);

				if (entry == null) {
					entry = new Entry(clazz);

					s_entries.put(clazz, entry);
				}
			}
		}

		return entry;
	}

	/**
	 * Default implementation. You should override this method for performance
	 * reason.
	 */
	@Override
	public int hashCode() {
		Entry entry = getEntry();
		int hash = 0;

		try {
			for (Field field : entry.getFields()) {
				if (entry.isKey(field)) {
					Object v1 = field.get(this);

					hash = hash * 31 + (v1 == null ? 0 : v1.hashCode());
				}
			}
		} catch (Exception e) {
			// ignore it
		}

		return hash;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		String str = Text.readString(in);

		PojoCodec.INSTANCE.decode(getEntry(), this, str);
	}

	@Override
	public String toString() {
		String str = PojoCodec.INSTANCE.encode(getEntry(), this);

		return str;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		String str = PojoCodec.INSTANCE.encode(getEntry(), this);

		Text.writeString(out, str);
	}

	static class Entry {
		private Class<?> m_clazz;

		private List<Field> m_fields;

		private Map<Field, Boolean> m_keys;

		public Entry(Class<?> clazz) {
			m_clazz = clazz;
			m_fields = new ArrayList<Field>();
			m_keys = new HashMap<Field, Boolean>();

			List<Integer> orders = new ArrayList<Integer>();
			boolean needsSort = false;

			for (Field field : clazz.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}

					m_fields.add(field);

					FieldMeta meta = field.getAnnotation(FieldMeta.class);

					if (meta != null) {
						m_keys.put(field, meta.key());
						orders.add(meta.order());

						if (!needsSort && meta.order() >= 0) {
							needsSort = true;
						}
					} else {
						orders.add(Integer.MAX_VALUE);
						m_keys.put(field, true);
					}
				}
			}

			if (needsSort) {
				sortFields(orders);
			}
		}

		public Class<?> getClazz() {
			return m_clazz;
		}

		public List<Field> getFields() {
			return m_fields;
		}

		public boolean isKey(Field field) {
			return m_keys.get(field);
		}

		private void sortFields(List<Integer> orders) {
			int len = orders.size();

			for (int i = 0; i < len; i++) {
				int o1 = orders.get(i);
				int index = 0;
				int o = o1;

				for (int j = i + 1; j < len; j++) {
					int o2 = orders.get(j);

					if (o > o2) {
						index = j;
						o = o2;
					}
				}

				if (index > 0) {
					orders.set(i, orders.get(index));
					orders.set(index, o1);

					Field f1 = m_fields.get(i);
					Field f2 = m_fields.get(index);

					m_fields.set(i, f2);
					m_fields.set(index, f1);
				}
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	protected static @interface FieldMeta {
		boolean key() default true;

		int order() default Integer.MAX_VALUE;
	}

	static enum PojoCodec {
		INSTANCE;

		public void decode(Entry entry, Object pojo, String data) {
			ValueInput input = new ValueInput(data);

			for (Field field : entry.getFields()) {
				if (input.next()) {
					String str = input.getValue();
					Class<?> type = field.getType();
					Object value = null;

					try {
						if (type == String.class) {
							value = str;
						} else if (type == Integer.class || type == Integer.TYPE) {
							value = Integer.valueOf(str);
						} else if (type == Long.class || type == Long.TYPE) {
							value = Boolean.valueOf(str);
						} else if (type == Double.class || type == Double.TYPE) {
							value = Double.valueOf(str);
						} else if (type == Boolean.class || type == Boolean.TYPE) {
							value = Boolean.valueOf(str);
						} else if (type == Class.class) {
							value = Class.forName(str);
						} else if (type == Float.class || type == Float.TYPE) {
							value = Float.valueOf(str);
						} else if (type == Short.class || type == Short.TYPE) {
							value = Short.valueOf(str);
						} else if (type == Character.class || type == Character.TYPE) {
							value = str.length() == 0 ? 0 : str.charAt(0);
						} else if (type == Byte.class || type == Byte.TYPE) {
							value = str.length() == 0 ? 0 : (byte) str.charAt(0);
						} else {
							value = str;
						}

						if (value != null) {
							field.set(pojo, value);
						}
					} catch (Exception e) {
						e.printStackTrace();
						// throw new
						// RuntimeException(String.format("Error when setting value(%s) to %s!",
						// value, field), e);
					}
				}
			}
		}

		public String encode(Entry entry, Object pojo) {
			StringBuilder sb = new StringBuilder(256);

			for (Field field : entry.getFields()) {
				if (sb.length() > 0) {
					sb.append('|');
				}

				try {
					Object value = field.get(pojo);

					if (value != null) {
						if (value instanceof Class) {
							escape(sb, ((Class<?>) value).getName());
						} else {
							escape(sb, value.toString());
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(String.format("Error when getting value from %s!", field), e);
				}
			}

			return sb.toString();
		}

		void escape(StringBuilder sb, String str) {
			int len = str.length();

			for (int i = 0; i < len; i++) {
				char ch = str.charAt(i);

				switch (ch) {
				case '\r':
					sb.append("\\r");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\\':
				case '|':
					sb.append('\\');
					sb.append(ch);
					break;
				default:
					sb.append(ch);
					break;
				}
			}
		}
	}

	static class ValueInput {
		private String m_source;

		private int m_index;

		private StringBuilder m_value;

		public ValueInput(String source) {
			m_source = source;
			m_value = new StringBuilder();
		}

		public String getValue() {
			return m_value.toString();
		}

		public boolean next() {
			int len = m_source.length();

			m_value.setLength(0);

			if (m_index >= len) {
				return false;
			}

			m_value.setLength(0);

			while (m_index < len) {
				char ch = m_source.charAt(m_index++);

				switch (ch) {
				case '|':
					return true;
				case '\\':
					if (m_index + 1 < len) {
						char ch2 = m_source.charAt(m_index++);

						if (ch2 == 'r') {
							ch = '\r';
						} else if (ch2 == 'n') {
							ch = '\n';
						} else {
							ch = ch2;
						}
					}
					// break through
				default:
					m_value.append(ch);
				}
			}

			return true;
		}
	}

}
