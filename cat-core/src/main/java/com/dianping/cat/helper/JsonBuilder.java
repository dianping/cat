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
package com.dianping.cat.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.unidal.lookup.annotation.Named;

@Named
public class JsonBuilder {

	private FieldNamingStrategy m_fieldNamingStrategy = new FieldNamingStrategy() {

		@Override
		public String translateName(Field f) {
			String name = f.getName();

			if (name.startsWith("m_")) {
				return name.substring(2);
			} else {
				return name;
			}
		}
	};

	private Gson m_gson = new GsonBuilder().registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter())
							.setDateFormat("yyyy-MM-dd HH:mm:ss").setFieldNamingStrategy(m_fieldNamingStrategy).create();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object parse(String json, Class clz) {
		return m_gson.fromJson(json, clz);
	}

	public String toJson(Object o) {
		return m_gson.toJson(o);
	}

	public class TimestampTypeAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
		private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
								throws JsonParseException {
			if (!(json instanceof JsonPrimitive)) {
				throw new JsonParseException("The date should be a string value");
			}

			try {
				Date date = format.parse(json.getAsString());
				return new Timestamp(date.getTime());
			} catch (ParseException e) {
				throw new JsonParseException(e);
			}
		}

		public JsonElement serialize(Timestamp src, Type arg1, JsonSerializationContext arg2) {
			String dateFormatAsString = format.format(new Date(src.getTime()));
			return new JsonPrimitive(dateFormatAsString);
		}
	}

}
