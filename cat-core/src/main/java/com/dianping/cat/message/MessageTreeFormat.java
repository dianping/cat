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
package com.dianping.cat.message;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;

public class MessageTreeFormat {

	public static void format(MessageTree tree) {
		try {
			formatTruncateMessage(tree);
			formatTransaction(tree);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private static void formatTransaction(MessageTree tree) {
		List<Transaction> transactions = tree.getTransactions();

		if (transactions != null) {
			for (Transaction t : transactions) {
				String type = t.getType();
				String name = t.getName();

				if ("URL".equals(type)) {
					modifyTransactionName(t, UrlParser.format(name));

					break;
				} else if ("System".equals(type) && name.startsWith("UploadMetric")) {
					modifyTransactionName(t, "UploadMetric");

					break;
				}
			}
		}
	}

	private static void formatTruncateMessage(MessageTree tree) {
		try {
			List<Event> events = tree.getEvents();

			if (events != null && !events.isEmpty()) {
				Event event = events.get(events.size() - 1);
				String type = event.getType();
				String name = event.getName();

				if ("TruncatedTransaction".equals(type) && "TotalDuration".equals(name)) {
					long delta = Long.parseLong(event.getData().toString());

					modifyDuration(tree, delta);
				}
			}
		} catch (NumberFormatException e) {
			// ignore
		}
	}

	private static void modifyDuration(MessageTree tree, long duration) {
		List<Transaction> transactions = tree.getTransactions();

		if (!transactions.isEmpty()) {
			transactions.get(0).setDurationInMillis(duration / 1000);

			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				((Transaction) message).setDurationInMillis(duration / 1000);
			}
		}
	}

	private static void modifyTransactionName(Transaction transaction, String name) {
		if (transaction instanceof DefaultTransaction) {
			((DefaultTransaction) transaction).setName(name);
		}
	}

	public static class UrlParser {
		public static final char SPLIT = '/';

		public static String format(String url) {
			int length = url.length();
			StringBuilder sb = new StringBuilder(length);

			for (int index = 0; index < length; ) {
				char c = url.charAt(index);

				if (c == SPLIT && index < length - 1) {
					sb.append(c);

					StringBuilder nextSection = new StringBuilder();
					boolean isNumber = false;
					boolean first = true;

					for (int j = index + 1; j < length; j++) {
						char next = url.charAt(j);

						if ((first || isNumber == true) && next != SPLIT) {
							isNumber = isNumber(next);
							first = false;
						}

						if (next == SPLIT) {
							if (isNumber) {
								sb.append("{num}");
							} else {
								sb.append(nextSection.toString());
							}
							index = j;

							break;
						} else if (j == length - 1) {
							if (isNumber) {
								sb.append("{num}");
							} else {
								nextSection.append(next);
								sb.append(nextSection.toString());
							}
							index = j + 1;
							break;
						} else {
							nextSection.append(next);
						}
					}
				} else {
					sb.append(c);
					index++;
				}
			}

			return sb.toString();
		}

		public static boolean isNumber(char c) {
			return (c >= '0' && c <= '9') || c == '.' || c == '-' || c == ',';
		}
	}

}
