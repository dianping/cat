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
package com.dianping.cat.util.json;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
	* A JSONObject is an unordered collection of name/value pairs. Its external form is a string wrapped in curly braces with colons
	* between the names and values, and commas between the values and names. The internal form is an object having get() and opt()
	* methods for accessing the values by name, and put() methods for adding or replacing values by name. The values can be any of
	* these types: Boolean, JSONArray, JSONObject, Number, String, or the JSONObject.NULL object.
	* <p>
	* The constructor can convert an external form string into an internal form Java object. The toString() method creates an external
	* form string.
	* <p>
	* A get() method returns a value if one can be found, and throws an exception if one cannot be found. An opt() method returns a
	* default value instead of throwing an exception, and so is useful for obtaining optional values.
	* <p>
	* The generic get() and opt() methods return an object, which you can cast or query for type. There are also typed get() and opt()
	* methods that do type checking and type coersion for you.
	* <p>
	* The texts produced by the toString() methods are very strict. The constructors are more forgiving in the texts they will accept:
	* <ul>
	* <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just before the closing brace.</li>
	* <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single quote)</small>.</li>
	* <li>Strings do not need to be quoted at all if they do not begin with a quote or single quote, and if they do not contain leading
	* or trailing spaces, and if they do not contain any of these characters: <code>{ } [ ] / \ : , = ; #</code> and if they do not
	* look like numbers and if they are not the reserved words <code>true</code>, <code>false</code>, or <code>null</code>.</li>
	* <li>Keys can be followed by <code>=</code> or <code>=></code> as well as by <code>:</code></li>
	* <li>Values can be followed by <code>;</code> as well as by <code>,</code></li>
	* <li>Numbers may have the <code>0-</code> <small>(octal)</small> or <code>0x-</code> <small>(hex)</small> prefix.</li>
	* <li>Line comments can begin with <code>#</code></li>
	* </ul>
	*
	* @author JSON.org
	* @version 1
	*/
public class JsonObject {

	/**
		* It is sometimes more convenient and less ambiguous to have a NULL object than to use Java's null value.
		* JSONObject.NULL.equals(null) returns true. JSONObject.NULL.toString() returns "null".
		*/
	public static final Object NULL = new Null();

	/**
		* The hash map where the JSONObject's properties are kept.
		*/
	private HashMap<String, Object> m_myHashMap;

	/**
		* Construct an empty JSONObject.
		*/
	public JsonObject() {
		m_myHashMap = new HashMap<String, Object>();
	}

	/**
		* Construct a JSONObject from a subset of another JSONObject. An array of strings is used to identify the keys that should be
		* copied. Missing keys are ignored.
		*
		* @param jo A JSONObject.
		* @param sa An array of strings.
		*/
	public JsonObject(JsonObject jo, String[] sa) {
		this();
		for (int i = 0; i < sa.length; i += 1) {
			putOpt(sa[i], jo.opt(sa[i]));
		}
	}

	/**
		* Construct a JSONObject from a JSONTokener.
		*
		* @param x A JSONTokener object containing the source string.
		* @throws ParseException if there is a syntax error in the source string.
		*/
	public JsonObject(JsonTokener x) throws ParseException {
		this();
		char c;
		String key;

		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject must begin with '{'");
		}
		while (true) {
			c = x.nextClean();
			switch (c) {
			case 0:
				throw x.syntaxError("A JSONObject must end with '}'");
			case '}':
				return;
			default:
				x.back();
				key = x.nextValue().toString();
			}

			/*
				* The key is followed by ':'. We will also tolerate '=' or '=>'.
			 */

			c = x.nextClean();
			if (c == '=') {
				if (x.next() != '>') {
					x.back();
				}
			} else if (c != ':') {
				throw x.syntaxError("Expected a ':' after a key");
			}
			m_myHashMap.put(key, x.nextValue());

			/*
			 * Pairs are separated by ','. We will also tolerate ';'.
			 */

			switch (x.nextClean()) {
			case ';':
			case ',':
				if (x.nextClean() == '}') {
					return;
				}
				x.back();
				break;
			case '}':
				return;
			default:
				throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
		* Construct a JSONObject from a Map.
		*
		* @param map A map object that can be used to initialize the contents of the JSONObject.
		*/
	public JsonObject(Map<String, Object> map) {
		m_myHashMap = new HashMap<String, Object>(map);
	}

	/**
		* Construct a JSONObject from a string. This is the most commonly used JSONObject constructor.
		*
		* @param string A string beginning with <code>{</code>&nbsp;<small>(left brace)</small> and ending with <code>}</code>
		*               &nbsp;<small>(right brace)</small>.
		* @throws ParseException The string must be properly formatted.
		*/
	public JsonObject(String string) throws ParseException {
		this(new JsonTokener(string));
	}

	/**
		* Produce a string from a number.
		*
		* @param n A Number
		* @return A String.
		* @throws ArithmeticException JSON can only serialize finite numbers.
		*/
	static public String numberToString(Number n) throws ArithmeticException {
		if ((n instanceof Float && (((Float) n).isInfinite() || ((Float) n).isNaN()))	|| (n instanceof Double && (
								((Double) n).isInfinite() || ((Double) n).isNaN()))) {
			throw new ArithmeticException("JSON can only serialize finite numbers.");
		}

		// Shave off trailing zeros and decimal point, if possible.

		String s = n.toString();
		if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while (s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if (s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	/**
		* Produce a string in double quotes with backslash sequences in all the right places.
		*
		* @param string A String
		* @return A String correctly formatted for insertion in a JSON message.
		*/
	public static String quote(String str) {
		int length = (str == null) ? 4 : str.length() + 4;
		StringBuilder sb = new StringBuilder(length);
		appendQuoted(str, sb);
		return sb.toString();
	}

	/**
		* Appends a string in double quotes with backslash sequences in all the right places.
		*
		* @param string A String
		* @return the string builder to which the quoted string is appended. It is the same string builder instance that is passed in.
		*/
	protected static StringBuilder appendQuoted(String str, StringBuilder sb) {
		if (str == null || str.length() == 0) {
			sb.append("\"\"");
			return sb;
		}

		sb.append('"');
		escape(str, false, true, sb);
		sb.append('"');
		return sb;
	}

	/**
		* Returns an estimate of the size of the string in the quoted form. The computation is actually simple; it pads 30% for the
		* escaping.
		*/
	protected static int getQuotedSize(String str) {
		if (str == null || str.length() == 0) {
			return 2;
		}
		// apply 30% padding for escaping
		return (int) (1.3 * str.length());
	}

	public static String escape(String string) {
		return escape(string, false);
	}

	public static String escape(String str, boolean singleQuote) {
		if (str == null || str.length() == 0) {
			return str;
		}
		StringBuilder sb = new StringBuilder(str.length());
		escape(str, singleQuote, false, sb);
		return sb.toString();
	}

	private static void escape(String str, boolean singleQuote, boolean handleSlash, StringBuilder sb) {
		char b;
		char c = 0;
		int len = str.length();
		String t;

		for (int i = 0; i < len; i++) {
			b = c;
			c = str.charAt(i);
			switch (c) {
			case '\\':
			case '"':
				if (!singleQuote) {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\'':
				if (singleQuote) {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '/':
				if (handleSlash && b == '<') {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\b':
				sb.append('\\').append('b');
				break;
			case '\t':
				sb.append('\\').append('t');
				break;
			case '\n':
				sb.append('\\').append('n');
				break;
			case '\f':
				sb.append('\\').append('f');
				break;
			case '\r':
				sb.append('\\').append('r');
				break;
			default:
				if (c < ' ') {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
	}

	/**
		* Make JSON string of an object value.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @param value The value to be serialized.
		* @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
		* brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
		*/
	static String valueToString(Object value) {
		if (value == null || value.equals(null)) { // KEEPME
			return "null";
		}
		if (value instanceof Number) {
			return numberToString((Number) value);
		}
		if (value instanceof Boolean || value instanceof JsonArray || JsonObject.class.isAssignableFrom(value.getClass())) {
			return value.toString();
		}
		if (!(value instanceof String)) {
			return value.toString();
		}
		return quote(value.toString());
	}

	/**
		* Appends the given value to the provided string builder. This is a companion method to valueToString(), but is provided as a
		* performance optimization by avoiding creating intermediate String objects. This form should be used over valueToString()
		* whenever possible.
		*
		* @param value the value to be appended.
		* @param sb    the string builder to which the value should be appended.
		* @return the original string builder instance.
		*/
	protected static StringBuilder appendValue(Object value, StringBuilder sb) {
		if (value == null || value.equals(null)) { // KEEPME
			return sb.append("null");
		}
		if (value instanceof String) {
			return appendQuoted((String) value, sb);
		}
		if (value instanceof Number) {
			return sb.append(numberToString((Number) value));
		}
		if (value instanceof Boolean) {
			return sb.append((Boolean) value);
		}
		if (value instanceof JsonArray) {
			return ((JsonArray) value).append(sb);
		}
		if (JsonObject.class.isAssignableFrom(value.getClass())) {
			return ((JsonObject) value).append(sb);
		}
		return sb.append(value.toString());
	}

	/**
		* Returns an estimate of the size of the value in a serialized form. This is provided to size the string builder appropriately.
		*/
	protected static int getValueSize(Object value) {
		if (value == null || value.equals(null)) { // KEEPME
			return 4;
		}
		// estimate
		if (value instanceof Number) {
			return 12;
		}
		if (value instanceof Boolean) {
			return 5;
		}
		if (value instanceof JsonArray) {
			return ((JsonArray) value).getSerializedSize();
		}
		if (JsonObject.class.isAssignableFrom(value.getClass())) {
			return ((JsonObject) value).getSerializedSize();
		}
		// calling toString() to compute the length is bit too much; take a guess
		if (!(value instanceof String)) {
			return 32;
		}
		return getQuotedSize((String) value);
	}

	/**
		* Make a prettyprinted JSON string of an object value.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @param value        The value to be serialized.
		* @param indentFactor The number of spaces to add to each level of indentation.
		* @param indent       The indentation of the top level.
		* @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
		* brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
		*/
	static String valueToString(Object value, int indentFactor, int indent) {
		if (value == null || value.equals(null)) { // KEEPME
			return "null";
		}
		if (value instanceof Number) {
			return numberToString((Number) value);
		}
		if (value instanceof Boolean) {
			return value.toString();
		}
		if (JsonObject.class.isAssignableFrom(value.getClass())) {
			return (((JsonObject) value).toString(indentFactor, indent));
		}
		if (value instanceof JsonArray) {
			return (((JsonArray) value).toString(indentFactor, indent));
		}
		return quote(value.toString());
	}

	/**
		* Accumulate values under a key. It is similar to the put method except that if there is already an object stored under the key
		* then a JSONArray is stored under the key to hold all of the accumulated values. If there is already a JSONArray, then the new
		* value is appended to it. In contrast, the put method replaces the previous value.
		*
		* @param key   A key string.
		* @param value An object to be accumulated under the key.
		* @return this.
		* @throws NullPointerException if the key is null
		*/
	public JsonObject accumulate(String key, Object value) throws NullPointerException {
		JsonArray a;
		Object o = opt(key);
		if (o == null) {
			put(key, value);
		} else if (o instanceof JsonArray) {
			a = (JsonArray) o;
			a.put(value);
		} else {
			a = new JsonArray();
			a.put(o);
			a.put(value);
			put(key, a);
		}
		return this;
	}

	/**
		* Get the value object associated with a key.
		*
		* @param key A key string.
		* @return The object associated with the key.
		* @throws NoSuchElementException if the key is not found.
		*/
	public Object get(String key) throws NoSuchElementException {
		Object o = opt(key);
		if (o == null) {
			throw new NoSuchElementException("JSONObject[" + quote(key) + "] not found.");
		}
		return o;
	}

	/**
		* Get the boolean value associated with a key.
		*
		* @param key A key string.
		* @return The truth.
		* @throws NoSuchElementException if the key is not found.
		* @throws ClassCastException     if the value is not a Boolean or the String "true" or "false".
		*/
	public boolean getBoolean(String key) throws ClassCastException, NoSuchElementException {
		Object o = get(key);
		if (o.equals(Boolean.FALSE) || (o instanceof String && ((String) o).equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new ClassCastException("JSONObject[" + quote(key) + "] is not a Boolean.");
	}

	/**
		* Get the double value associated with a key.
		*
		* @param key A key string.
		* @return The numeric value.
		* @throws NumberFormatException  if the value cannot be converted to a number.
		* @throws NoSuchElementException if the key is not found or if the value is a Number object.
		*/
	public double getDouble(String key) throws NoSuchElementException, NumberFormatException {
		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof String) {
			return new Double((String) o).doubleValue();
		}
		throw new NumberFormatException("JSONObject[" + quote(key) + "] is not a number.");
	}

	/**
		* Get the long value associated with a key.
		*
		* @param key A key string.
		* @return The numeric value.
		* @throws NumberFormatException  if the value cannot be converted to a number.
		* @throws NoSuchElementException if the key is not found or if the value is a Number object.
		*/
	public long getLong(String key) throws NoSuchElementException, NumberFormatException {
		Object o = get(key);
		if (o instanceof Number) {
			return ((Number) o).longValue();
		}
		if (o instanceof String) {
			return new Long((String) o).longValue();
		}
		throw new NumberFormatException("JSONObject[" + quote(key) + "] is not a number.");
	}

	/**
		* Get the HashMap the holds that contents of the JSONObject.
		*
		* @return The getHashMap.
		*/
	HashMap<String, Object> getHashMap() {
		return m_myHashMap;
	}

	/**
		* Get the int value associated with a key.
		*
		* @param key A key string.
		* @return The integer value.
		* @throws NoSuchElementException if the key is not found
		* @throws NumberFormatException  if the value cannot be converted to a number.
		*/
	public int getInt(String key) throws NoSuchElementException, NumberFormatException {
		Object o = get(key);
		return o instanceof Number ? ((Number) o).intValue() : (int) getDouble(key);
	}

	/**
		* Get the JSONArray value associated with a key.
		*
		* @param key A key string.
		* @return A JSONArray which is the value.
		* @throws NoSuchElementException if the key is not found or if the value is not a JSONArray.
		*/
	public JsonArray getJSONArray(String key) throws NoSuchElementException {
		Object o = get(key);
		if (o instanceof JsonArray) {
			return (JsonArray) o;
		}
		throw new NoSuchElementException("JSONObject[" + quote(key) + "] is not a JSONArray.");
	}

	/**
		* Get the JSONObject value associated with a key.
		*
		* @param key A key string.
		* @return A JSONObject which is the value.
		* @throws NoSuchElementException if the key is not found or if the value is not a JSONObject.
		*/
	public JsonObject getJSONObject(String key) throws NoSuchElementException {
		Object o = get(key);
		if (o instanceof JsonObject) {
			return (JsonObject) o;
		}
		throw new NoSuchElementException("JSONObject[" + quote(key) + "] is not a JSONObject.");
	}

	/**
		* Get the string associated with a key.
		*
		* @param key A key string.
		* @return A string which is the value.
		* @throws NoSuchElementException if the key is not found.
		*/
	public String getString(String key) throws NoSuchElementException {
		return get(key).toString();
	}

	/**
		* Determine if the JSONObject contains a specific key.
		*
		* @param key A key string.
		* @return true if the key exists in the JSONObject.
		*/
	public boolean has(String key) {
		return this.m_myHashMap.containsKey(key);
	}

	/**
		* Determine if the value associated with the key is null or if there is no value.
		*
		* @param key A key string.
		* @return true if there is no value associated with the key or if the value is the JSONObject.NULL object.
		*/
	public boolean isNull(String key) {
		return JsonObject.NULL.equals(opt(key));
	}

	/**
		* Get an enumeration of the keys of the JSONObject.
		*
		* @return An iterator of the keys.
		*/
	public Iterator<String> keys() {
		return this.m_myHashMap.keySet().iterator();
	}

	/**
		* Get the number of keys stored in the JSONObject.
		*
		* @return The number of keys in the JSONObject.
		*/
	public int length() {
		return this.m_myHashMap.size();
	}

	/**
		* Produce a JSONArray containing the names of the elements of this JSONObject.
		*
		* @return A JSONArray containing the key strings, or null if the JSONObject is empty.
		*/
	public JsonArray names() {
		JsonArray ja = new JsonArray();
		Iterator<String> keys = keys();
		while (keys.hasNext()) {
			ja.put(keys.next());
		}
		return ja.length() == 0 ? null : ja;
	}

	/**
		* Get an optional value associated with a key.
		*
		* @param key A key string.
		* @return An object which is the value, or null if there is no value.
		* @throws NullPointerException The key must not be null.
		*/
	public Object opt(String key) throws NullPointerException {
		if (key == null) {
			throw new NullPointerException("Null key");
		}
		return this.m_myHashMap.get(key);
	}

	/**
		* Get an optional boolean associated with a key. It returns false if there is no such key, or if the value is not Boolean.TRUE
		* or the String "true".
		*
		* @param key A key string.
		* @return The truth.
		*/
	public boolean optBoolean(String key) {
		return optBoolean(key, false);
	}

	/**
		* Get an optional boolean associated with a key. It returns the defaultValue if there is no such key, or if it is not a Boolean
		* or the String "true" or "false" (case insensitive).
		*
		* @param key          A key string.
		* @param defaultValue The default.
		* @return The truth.
		*/
	public boolean optBoolean(String key, boolean defaultValue) {
		Object o = opt(key);
		if (o != null) {
			if (o.equals(Boolean.FALSE) || (o instanceof String && ((String) o).equalsIgnoreCase("false"))) {
				return false;
			} else if (o.equals(Boolean.TRUE) || (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
				return true;
			}
		}
		return defaultValue;
	}

	/**
		* Get an optional double associated with a key, or NaN if there is no such key or if its value is not a number. If the value is
		* a string, an attempt will be made to evaluate it as a number.
		*
		* @param key A string which is the key.
		* @return An object which is the value.
		*/
	public double optDouble(String key) {
		return optDouble(key, Double.NaN);
	}

	/**
		* Get an optional double associated with a key, or the defaultValue if there is no such key or if its value is not a number. If
		* the value is a string, an attempt will be made to evaluate it as a number.
		*
		* @param key          A key string.
		* @param defaultValue The default.
		* @return An object which is the value.
		*/
	public double optDouble(String key, double defaultValue) {
		Object o = opt(key);
		if (o != null) {
			if (o instanceof Number) {
				return ((Number) o).doubleValue();
			}
			try {
				return new Double((String) o).doubleValue();
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
		* Get an optional int value associated with a key, or zero if there is no such key or if the value is not a number. If the value
		* is a string, an attempt will be made to evaluate it as a number.
		*
		* @param key A key string.
		* @return An object which is the value.
		*/
	public int optInt(String key) {
		return optInt(key, 0);
	}

	/**
		* Get an optional int value associated with a key, or the default if there is no such key or if the value is not a number. If
		* the value is a string, an attempt will be made to evaluate it as a number.
		*
		* @param key          A key string.
		* @param defaultValue The default.
		* @return An object which is the value.
		*/
	public int optInt(String key, int defaultValue) {
		Object o = opt(key);
		if (o != null) {
			if (o instanceof Number) {
				return ((Number) o).intValue();
			}
			try {
				return Integer.parseInt((String) o);
			} catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
		* Get an optional JSONArray associated with a key. It returns null if there is no such key, or if its value is not a JSONArray.
		*
		* @param key A key string.
		* @return A JSONArray which is the value.
		*/
	public JsonArray optJSONArray(String key) {
		Object o = opt(key);
		return o instanceof JsonArray ? (JsonArray) o : null;
	}

	/**
		* Get an optional JSONObject associated with a key. It returns null if there is no such key, or if its value is not a
		* JSONObject.
		*
		* @param key A key string.
		* @return A JSONObject which is the value.
		*/
	public JsonObject optJSONObject(String key) {
		Object o = opt(key);
		return o instanceof JsonObject ? (JsonObject) o : null;
	}

	/**
		* Get an optional string associated with a key. It returns an empty string if there is no such key. If the value is not a string
		* and is not null, then it is coverted to a string.
		*
		* @param key A key string.
		* @return A string which is the value.
		*/
	public String optString(String key) {
		return optString(key, "");
	}

	/**
		* Get an optional string associated with a key. It returns the defaultValue if there is no such key.
		*
		* @param key          A key string.
		* @param defaultValue The default.
		* @return A string which is the value.
		*/
	public String optString(String key, String defaultValue) {
		Object o = opt(key);
		return o != null ? o.toString() : defaultValue;
	}

	/**
		* Put a key/boolean pair in the JSONObject.
		*
		* @param key   A key string.
		* @param value A boolean which is the value.
		* @return this.
		*/
	public JsonObject put(String key, boolean value) {
		put(key, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
		* Put a key/double pair in the JSONObject.
		*
		* @param key   A key string.
		* @param value A double which is the value.
		* @return this.
		*/
	public JsonObject put(String key, double value) {
		put(key, new Double(value));
		return this;
	}

	/**
		* Put a key/int pair in the JSONObject.
		*
		* @param key   A key string.
		* @param value An int which is the value.
		* @return this.
		*/
	public JsonObject put(String key, int value) {
		put(key, new Integer(value));
		return this;
	}

	/**
		* Put a key/value pair in the JSONObject. If the value is null, then the key will be removed from the JSONObject if it is
		* present.
		*
		* @param key   A key string.
		* @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer, JSONArray, JSONObject,
		*              String, or the JSONObject.NULL object.
		* @return this.
		* @throws NullPointerException The key must be non-null.
		*/
	public JsonObject put(String key, Object value) throws NullPointerException {
		if (key == null) {
			throw new NullPointerException("Null key.");
		}
		if (value != null) {
			this.m_myHashMap.put(key, value);
		} else {
			remove(key);
		}
		return this;
	}

	/**
		* Put a key/value pair in the JSONObject, but only if the value is non-null.
		*
		* @param key   A key string.
		* @param value An object which is the value. It should be of one of these types: Boolean, Double, Integer, JSONArray, JSONObject,
		*              String, or the JSONObject.NULL object.
		* @return this.
		* @throws NullPointerException The key must be non-null.
		*/
	public JsonObject putOpt(String key, Object value) throws NullPointerException {
		if (value != null) {
			put(key, value);
		}
		return this;
	}

	/**
		* Remove a name and its value, if present.
		*
		* @param key The name to be removed.
		* @return The value that was associated with the name, or null if there was no value.
		*/
	public Object remove(String key) {
		return this.m_myHashMap.remove(key);
	}

	/**
		* Produce a JSONArray containing the values of the members of this JSONObject.
		*
		* @param names A JSONArray containing a list of key strings. This determines the sequence of the values in the result.
		* @return A JSONArray of values.
		*/
	public JsonArray toJSONArray(JsonArray names) {
		if (names == null || names.length() == 0) {
			return null;
		}
		JsonArray ja = new JsonArray();
		for (int i = 0; i < names.length(); i += 1) {
			ja.put(this.opt(names.getString(i)));
		}
		return ja;
	}

	/**
		* Make an JSON external form string of this JSONObject. For compactness, no unnecessary whitespace is added.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		* <p>
		* Warning: whenever toString() is overridden, you must override append() as well. Not overriding append() will result in
		* incorrect serialization results. The append() method should contain the actual concatenation logic and toString() should
		* simply utilize append(). An example of overriding both methods is as follows:
		* <p>
		* <p>
		* <pre>
		* &#064;Override
		* public String toString() {
		* 	return append(new StringBuilder()).toString();
		* }
		*
		* &#064;Override
		* public StringBuilder append(StringBuilder sb) {
		* 	sb.append(&quot;{value=&quot;);
		* 	sb.append(value);
		* 	sb.append('}');
		* 	return sb;
		* }
		* </pre>
		*
		* @return a printable, displayable, portable, transmittable representation of the object, beginning with <code>{</code>
		* &nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
		*/
	public String toString() {
		StringBuilder sb = new StringBuilder(getSerializedSize());
		return append(sb).toString();
	}

	/**
		* Appends this JsonObject to the provided string builder. This is a companion method to toString(), and is provided as a better
		* way of serializing the object. It avoids creating intermediate String objects in the course of serialization.
		* <p>
		* Warning: whenever toString() is overridden, you must override append() as well. Not overriding append() will result in
		* incorrect serialization results. The append() method should contain the actual concatenation logic and toString() should
		* simply utilize append(). An example of overriding both methods is as follows:
		* <p>
		* <p>
		* <pre>
		* &#064;Override
		* public String toString() {
		* 	return append(new StringBuilder()).toString();
		* }
		*
		* &#064;Override
		* public StringBuilder append(StringBuilder sb) {
		* 	sb.append(&quot;{value=&quot;);
		* 	sb.append(value);
		* 	sb.append('}');
		* 	return sb;
		* }
		* </pre>
		*/
	public StringBuilder append(StringBuilder sb) {
		sb.append('{');
		boolean firstEntry = true;
		for (Map.Entry<String, Object> entry : m_myHashMap.entrySet()) {
			if (!firstEntry) {
				sb.append(',');
			} else {
				firstEntry = false;
			}
			appendQuoted(entry.getKey(), sb);
			sb.append(':');
			appendValue(entry.getValue(), sb);
		}
		sb.append('}');
		return sb;
	}

	/**
		* Returns an estimate of the size of the JsonObject in a fully serialized form. This is provided to size the string builder
		* appropriately.
		*/
	private int getSerializedSize() {
		int value = 2 + length() * 2;
		for (Map.Entry<String, Object> entry : m_myHashMap.entrySet()) {
			value += getQuotedSize(entry.getKey()) + getValueSize(entry.getValue());
		}
		// add padding to overshoot a little
		return (int) (1.2 * value);
	}

	/**
		* Make a prettyprinted JSON external form string of this JSONObject.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @param indentFactor The number of spaces to add to each level of indentation.
		* @return a printable, displayable, portable, transmittable representation of the object, beginning with <code>{</code>
		* &nbsp;<small>(left brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
		*/
	public String toString(int indentFactor) {
		return toString(indentFactor, 0);
	}

	/**
		* Make a prettyprinted JSON string of this JSONObject.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @param indentFactor The number of spaces to add to each level of indentation.
		* @param indent       The indentation of the top level.
		* @return a printable, displayable, transmittable representation of the object, beginning with <code>{</code>&nbsp;<small>(left
		* brace)</small> and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
		*/
	String toString(int indentFactor, int indent) {
		int i;
		int n = length();
		if (n == 0) {
			return "{}";
		}
		Iterator<String> keys = keys();
		StringBuilder sb = new StringBuilder("{");
		int newindent = indent + indentFactor;
		String key;
		if (n == 1) {
			key = keys.next();
			sb.append(quote(key));
			sb.append(": ");
			sb.append(valueToString(this.m_myHashMap.get(key), indentFactor, indent));
		} else {
			while (keys.hasNext()) {
				key = keys.next();
				if (sb.length() > 1) {
					sb.append(",\n");
				} else {
					sb.append('\n');
				}
				for (i = 0; i < newindent; i += 1) {
					sb.append(' ');
				}
				sb.append(quote(key));
				sb.append(": ");
				sb.append(valueToString(this.m_myHashMap.get(key), indentFactor, newindent));
			}
			if (sb.length() > 1) {
				sb.append('\n');
				for (i = 0; i < indent; i += 1) {
					sb.append(' ');
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}

	/**
		* JSONObject.NULL is equivalent to the value that JavaScript calls null, whilst Java's null is equivalent to the value that
		* JavaScript calls undefined.
		*/
	private static final class Null {

		/**
			* There is only intended to be a single instance of the NULL object, so the clone method returns itself.
			*
			* @return NULL.
			*/
		protected final Object clone() {
			return this;
		}

		/**
			* A Null object is equal to the null value and to itself.
			*
			* @param object An object to test for nullness.
			* @return true if the object parameter is the JSONObject.NULL object or null.
			*/
		public boolean equals(Object object) {
			return object == null || object == this;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		/**
			* Get the "null" string value.
			*
			* @return The string "null".
			*/
		public String toString() {
			return "null";
		}
	}
}