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
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
	* A JSONArray is an ordered sequence of values. Its external form is a string
	* wrapped in square brackets with commas between the values. The internal form
	* is an object having get() and opt() methods for accessing the values by
	* index, and put() methods for adding or replacing values. The values can be
	* any of these types: Boolean, JSONArray, JSONObject, Number, String, or the
	* JSONObject.NULL object.
	* <p>
	* The constructor can convert a JSON external form string into an
	* internal form Java object. The toString() method creates an external
	* form string.
	* <p>
	* A get() method returns a value if one can be found, and throws an exception
	* if one cannot be found. An opt() method returns a default value instead of
	* throwing an exception, and so is useful for obtaining optional values.
	* <p>
	* The generic get() and opt() methods return an object which you can cast or
	* query for type. There are also typed get() and opt() methods that do typing
	* checking and type coersion for you.
	* <p>
	* The texts produced by the toString() methods are very strict.
	* The constructors are more forgiving in the texts they will accept.
	* <ul>
	* <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
	* before the closing bracket.</li>
	* <li>The null value will be inserted when there
	* is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
	* <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
	* quote)</small>.</li>
	* <li>Strings do not need to be quoted at all if they do not begin with a quote
	* or single quote, and if they do not contain leading or trailing spaces,
	* and if they do not contain any of these characters:
	* <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
	* and if they are not the reserved words <code>true</code>,
	* <code>false</code>, or <code>null</code>.</li>
	* <li>Values can be followed by <code>;</code> as well as by <code>,</code></li>
	* <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
	* <code>0x-</code> <small>(hex)</small> prefix.</li>
	* <li>Line comments can begin with <code>#</code></li>
	* </ul>
	*
	* @author JSON.org
	* @version 1
	*/
public class JsonArray {

	/**
		* The getArrayList where the JSONArray's properties are kept.
		*/
	private ArrayList<Object> m_myArrayList;

	/**
		* Construct an empty JSONArray.
		*/
	public JsonArray() {
		m_myArrayList = new ArrayList<Object>();
	}

	/**
		* Construct a JSONArray from a JSONTokener.
		*
		* @param x A JSONTokener
		* @throws ParseException A JSONArray must start with '['
		* @throws ParseException Expected a ',' or ']'
		*/
	public JsonArray(JsonTokener x) throws ParseException {
		this();
		if (x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray must start with '['");
		}
		if (x.nextClean() == ']') {
			return;
		}
		x.back();
		while (true) {
			if (x.nextClean() == ',') {
				x.back();
				m_myArrayList.add(null);
			} else {
				x.back();
				m_myArrayList.add(x.nextValue());
			}
			switch (x.nextClean()) {
			case ';':
			case ',':
				if (!x.more()) {
					throw x.syntaxError("Expected a ']'");
				}
				if (x.nextClean() == ']') {
					return;
				}
				x.back();
				break;
			case ']':
				return;
			default:
				throw x.syntaxError("Expected a ',' or ']'");
			}
		}
	}

	/**
		* Construct a JSONArray from a source string.
		*
		* @param string A string that begins with
		*               <code>[</code>&nbsp;<small>(left bracket)</small>
		*               and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
		* @throws ParseException The string must conform to JSON syntax.
		*/
	public JsonArray(String string) throws ParseException {
		this(new JsonTokener(string));
	}

	/**
		* Construct a JSONArray from a Collection.
		*
		* @param collection A Collection.
		*/
	public JsonArray(Collection<Object> collection) {
		m_myArrayList = new ArrayList<Object>(collection);
	}

	/**
		* Get the object value associated with an index.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return An object value.
		* @throws NoSuchElementException
		*/
	public Object get(int index) throws NoSuchElementException {
		Object o = opt(index);
		if (o == null) {
			throw new NoSuchElementException("JSONArray[" + index +	"] not found.");
		}
		return o;
	}

	/**
		* Get the ArrayList which is holding the elements of the JSONArray.
		*
		* @return The ArrayList.
		*/
	ArrayList<Object> getArrayList() {
		return m_myArrayList;
	}

	/**
		* Get the boolean value associated with an index.
		* The string values "true" and "false" are converted to boolean.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return The truth.
		* @throws NoSuchElementException if the index is not found
		* @throws ClassCastException
		*/
	public boolean getBoolean(int index)	throws ClassCastException, NoSuchElementException {
		Object o = get(index);
		if (o.equals(Boolean.FALSE) ||	(o instanceof String &&	((String) o).equalsIgnoreCase("false"))) {
			return false;
		} else if (o.equals(Boolean.TRUE) ||	(o instanceof String &&	((String) o).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new ClassCastException("JSONArray[" + index +	"] not a Boolean.");
	}

	/**
		* Get the double value associated with an index.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return The value.
		* @throws NoSuchElementException if the key is not found
		* @throws NumberFormatException  if the value cannot be converted to a number.
		*/
	public double getDouble(int index)	throws NoSuchElementException, NumberFormatException {
		Object o = get(index);
		if (o instanceof Number) {
			return ((Number) o).doubleValue();
		}
		if (o instanceof String) {
			return new Double((String) o).doubleValue();
		}
		throw new NumberFormatException("JSONObject[" +	index + "] is not a number.");
	}

	/**
		* Get the int value associated with an index.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return The value.
		* @throws NoSuchElementException if the key is not found
		* @throws NumberFormatException  if the value cannot be converted to a number.
		*/
	public int getInt(int index)	throws NoSuchElementException, NumberFormatException {
		Object o = get(index);
		return o instanceof Number ? ((Number) o).intValue() :	(int) getDouble(index);
	}

	/**
		* Get the JSONArray associated with an index.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return A JSONArray value.
		* @throws NoSuchElementException if the index is not found or if the
		*                                value is not a JSONArray
		*/
	public JsonArray getJSONArray(int index) throws NoSuchElementException {
		Object o = get(index);
		if (o instanceof JsonArray) {
			return (JsonArray) o;
		}
		throw new NoSuchElementException("JSONArray[" + index +	"] is not a JSONArray.");
	}

	/**
		* Get the JSONObject associated with an index.
		*
		* @param index subscript
		* @return A JSONObject value.
		* @throws NoSuchElementException if the index is not found or if the
		*                                value is not a JSONObject
		*/
	public JsonObject getJSONObject(int index) throws NoSuchElementException {
		Object o = get(index);
		if (o instanceof JsonObject) {
			return (JsonObject) o;
		}
		throw new NoSuchElementException("JSONArray[" + index +	"] is not a JSONObject.");
	}

	/**
		* Get the string associated with an index.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return A string value.
		* @throws NoSuchElementException
		*/
	public String getString(int index) throws NoSuchElementException {
		return get(index).toString();
	}

	/**
		* Determine if the value is null.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return true if the value at the index is null, or if there is no value.
		*/
	public boolean isNull(int index) {
		Object o = opt(index);
		return o == null || o.equals(null);
	}

	/**
		* Make a string from the contents of this JSONArray. The separator string
		* is inserted between each element.
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @param separator A string that will be inserted between the elements.
		* @return a string.
		*/
	public String join(String separator) {
		StringBuilder sb = new StringBuilder(getSerializedSize());
		return appendJoin(separator, sb).toString();
	}

	/**
		* Appends this JsonArray to the provided string builder.  The separator
		* string is inserted between each element.  This is a companion method to
		* the join() method.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @param separator A string that will be inserted between the elements.
		* @return the original string builder.
		*/
	private StringBuilder appendJoin(String separator, StringBuilder sb) {
		int len = length();
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			JsonObject.appendValue(m_myArrayList.get(i), sb);
		}
		return sb;
	}

	/**
		* Get the length of the JSONArray.
		*
		* @return The length (or size).
		*/
	public int length() {
		return m_myArrayList.size();
	}

	/**
		* Get the optional object value associated with an index.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return An object value, or null if there is no
		* object at that index.
		*/
	public Object opt(int index) {
		return index < 0 || index >= length() ?	null :	m_myArrayList.get(index);
	}

	/**
		* Get the optional boolean value associated with an index.
		* It returns false if there is no value at that index,
		* or if the value is not Boolean.TRUE or the String "true".
		*
		* @param index The index must be between 0 and length() - 1.
		* @return The truth.
		*/
	public boolean optBoolean(int index) {
		return optBoolean(index, false);
	}

	/**
		* Get the optional boolean value associated with an index.
		* It returns the defaultValue if there is no value at that index or if it is not
		* a Boolean or the String "true" or "false" (case insensitive).
		*
		* @param index        The index must be between 0 and length() - 1.
		* @param defaultValue A boolean default.
		* @return The truth.
		*/
	public boolean optBoolean(int index, boolean defaultValue) {
		Object o = opt(index);
		if (o != null) {
			if (o.equals(Boolean.FALSE) ||	(o instanceof String &&	((String) o).equalsIgnoreCase("false"))) {
				return false;
			} else if (o.equals(Boolean.TRUE) ||	(o instanceof String &&	((String) o).equalsIgnoreCase("true"))) {
				return true;
			}
		}
		return defaultValue;
	}

	/**
		* Get the optional double value associated with an index.
		* NaN is returned if the index is not found,
		* or if the value is not a number and cannot be converted to a number.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return The value.
		*/
	public double optDouble(int index) {
		return optDouble(index, Double.NaN);
	}

	/**
		* Get the optional double value associated with an index.
		* The defaultValue is returned if the index is not found,
		* or if the value is not a number and cannot be converted to a number.
		*
		* @param index        subscript
		* @param defaultValue The default value.
		* @return The value.
		*/
	public double optDouble(int index, double defaultValue) {
		Object o = opt(index);
		if (o != null) {
			if (o instanceof Number) {
				return ((Number) o).doubleValue();
			}
			try {
				return new Double((String) o).doubleValue();
			}	catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
		* Get the optional int value associated with an index.
		* Zero is returned if the index is not found,
		* or if the value is not a number and cannot be converted to a number.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return The value.
		*/
	public int optInt(int index) {
		return optInt(index, 0);
	}

	/**
		* Get the optional int value associated with an index.
		* The defaultValue is returned if the index is not found,
		* or if the value is not a number and cannot be converted to a number.
		*
		* @param index        The index must be between 0 and length() - 1.
		* @param defaultValue The default value.
		* @return The value.
		*/
	public int optInt(int index, int defaultValue) {
		Object o = opt(index);
		if (o != null) {
			if (o instanceof Number) {
				return ((Number) o).intValue();
			}
			try {
				return Integer.parseInt((String) o);
			}	catch (Exception e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	/**
		* Get the optional JSONArray associated with an index.
		*
		* @param index subscript
		* @return A JSONArray value, or null if the index has no value,
		* or if the value is not a JSONArray.
		*/
	public JsonArray optJSONArray(int index) {
		Object o = opt(index);
		return o instanceof JsonArray ? (JsonArray) o : null;
	}

	/**
		* Get the optional JSONObject associated with an index.
		* Null is returned if the key is not found, or null if the index has
		* no value, or if the value is not a JSONObject.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return A JSONObject value.
		*/
	public JsonObject optJSONObject(int index) {
		Object o = opt(index);
		return o instanceof JsonObject ? (JsonObject) o : null;
	}

	/**
		* Get the optional string value associated with an index. It returns an
		* empty string if there is no value at that index. If the value
		* is not a string and is not null, then it is coverted to a string.
		*
		* @param index The index must be between 0 and length() - 1.
		* @return A String value.
		*/
	public String optString(int index) {
		return optString(index, "");
	}

	/**
		* Get the optional string associated with an index.
		* The defaultValue is returned if the key is not found.
		*
		* @param index        The index must be between 0 and length() - 1.
		* @param defaultValue The default value.
		* @return A String value.
		*/
	public String optString(int index, String defaultValue) {
		Object o = opt(index);
		return o != null ? o.toString() : defaultValue;
	}

	/**
		* Append a boolean value.
		*
		* @param value A boolean value.
		* @return this.
		*/
	public JsonArray put(boolean value) {
		put(value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
		* Append a double value.
		*
		* @param value A double value.
		* @return this.
		*/
	public JsonArray put(double value) {
		put(new Double(value));
		return this;
	}

	/**
		* Append an int value.
		*
		* @param value An int value.
		* @return this.
		*/
	public JsonArray put(int value) {
		put(new Integer(value));
		return this;
	}

	/**
		* Append an object value.
		*
		* @param value An object value.  The value should be a
		*              Boolean, Double, Integer, JSONArray, JSObject, or String, or the
		*              JSONObject.NULL object.
		* @return this.
		*/
	public JsonArray put(Object value) {
		m_myArrayList.add(value);
		return this;
	}

	/**
		* Put or replace a boolean value in the JSONArray.
		*
		* @param index subscript The subscript. If the index is greater than the length of
		*              the JSONArray, then null elements will be added as necessary to pad
		*              it out.
		* @param value A boolean value.
		* @return this.
		* @throws NoSuchElementException The index must not be negative.
		*/
	public JsonArray put(int index, boolean value) {
		put(index, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/**
		* Put or replace a double value.
		*
		* @param index subscript The subscript. If the index is greater than the length of
		*              the JSONArray, then null elements will be added as necessary to pad
		*              it out.
		* @param value A double value.
		* @return this.
		* @throws NoSuchElementException The index must not be negative.
		*/
	public JsonArray put(int index, double value) {
		put(index, new Double(value));
		return this;
	}

	/**
		* Put or replace an int value.
		*
		* @param index subscript The subscript. If the index is greater than the
		*              length of the JSONArray, then null elements will be added as necessary
		*              to pad it out.
		* @param value An int value.
		* @return this.
		* @throws NoSuchElementException The index must not be negative.
		*/
	public JsonArray put(int index, int value) {
		put(index, new Integer(value));
		return this;
	}

	/**
		* Put or replace an object value in the JSONArray.
		*
		* @param index The subscript. If the index is greater than the length of
		*              the JSONArray, then null elements will be added as necessary to pad
		*              it out.
		* @param value An object value.
		* @return this.
		* @throws NoSuchElementException The index must not be negative.
		* @throws NullPointerException   The index must not be null.
		*/
	public JsonArray put(int index, Object value)	throws NoSuchElementException, NullPointerException {
		if (index < 0) {
			throw new NoSuchElementException("JSONArray[" + index +	"] not found.");
		} else if (value == null) {
			throw new NullPointerException();
		} else if (index < length()) {
			m_myArrayList.set(index, value);
		} else {
			while (index != length()) {
				put(null);
			}
			put(value);
		}
		return this;
	}

	/**
		* Produce a JSONObject by combining a JSONArray of names with the values
		* of this JSONArray.
		*
		* @param names A JSONArray containing a list of key strings. These will be
		*              paired with the values.
		* @return A JSONObject, or null if there are no names or if this JSONArray
		* has no values.
		*/
	public JsonObject toJSONObject(JsonArray names) {
		if (names == null || names.length() == 0 || length() == 0) {
			return null;
		}
		JsonObject jo = new JsonObject();
		for (int i = 0; i < names.length(); i += 1) {
			jo.put(names.getString(i), this.opt(i));
		}
		return jo;
	}

	/**
		* Make an JSON external form string of this JSONArray. For compactness, no
		* unnecessary whitespace is added.
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @return a printable, displayable, transmittable
		* representation of the array.
		*/
	public String toString() {
		StringBuilder sb = new StringBuilder(getSerializedSize());
		return append(sb).toString();
	}

	/**
		* Appends the JSON external form of this JSONArray to the provided string
		* builder. For compactness, no unnecessary whitespace is added.  This is a
		* companion method to the toString() method.
		* <p>
		* Warning: This method assumes that the data structure is acyclical.
		*
		* @return the original string builder.
		*/
	StringBuilder append(StringBuilder sb) {
		sb.append('[');
		appendJoin(",", sb);
		sb.append(']');
		return sb;
	}

	/**
		* Returns an estimate of the size of the JsonArray in a fully serialized
		* form.  This is provided to size the string builder appropriately.
		*/
	int getSerializedSize() {
		int arraySize = length();
		int value = 2 + arraySize;
		for (Object obj : m_myArrayList) {
			value += JsonObject.getValueSize(obj);
		}
		// add padding to overshoot a little
		return (int) (1.2 * value);
	}

	/**
		* Make a prettyprinted JSON string of this JSONArray.
		* Warning: This method assumes that the data structure is non-cyclical.
		*
		* @param indentFactor The number of spaces to add to each level of
		*                     indentation.
		* @return a printable, displayable, transmittable
		* representation of the object, beginning
		* with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
		* with <code>]</code>&nbsp;<small>(right bracket)</small>.
		*/
	public String toString(int indentFactor) {
		return toString(indentFactor, 0);
	}

	/**
		* Make a prettyprinted string of this JSONArray.
		* Warning: This method assumes that the data structure is non-cyclical.
		*
		* @param indentFactor The number of spaces to add to each level of
		*                     indentation.
		* @param indent       The indention of the top level.
		* @return a printable, displayable, transmittable
		* representation of the array.
		*/
	String toString(int indentFactor, int indent) {
		int len = length();
		if (len == 0) {
			return "[]";
		}
		int i;
		StringBuilder sb = new StringBuilder("[");
		if (len == 1) {
			sb.append(JsonObject.valueToString(m_myArrayList.get(0),	indentFactor, indent));
		} else {
			int newindent = indent + indentFactor;
			sb.append('\n');
			for (i = 0; i < len; i += 1) {
				if (i > 0) {
					sb.append(",\n");
				}
				for (int j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(JsonObject.valueToString(m_myArrayList.get(i),	indentFactor, newindent));
			}
			sb.append('\n');
			for (i = 0; i < indent; i += 1) {
				sb.append(' ');
			}
		}
		sb.append(']');
		return sb.toString();
	}
}