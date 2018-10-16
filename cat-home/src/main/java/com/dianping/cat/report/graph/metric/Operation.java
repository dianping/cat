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
package com.dianping.cat.report.graph.metric;

import java.util.ListIterator;
import java.util.Stack;

public class Operation {

	private String src;

	public Operation(String src) {
		this.src = src;
	}

	public double calculate(double a, double b, char op) {
		switch (op) {
		case '+':
			return a + b;
		case '-':
			return a - b;
		case '*':
			return a * b;
		case '/':
			return a / b;
		}
		return -1;
	}

	private String getPostfix() {
		Stack<String> stk = new Stack<String>();
		String postfix = new String();
		char op;
		int i = 0;
		while (i < src.length()) {
			if (Character.isDigit(src.charAt(i)) || src.charAt(i) == '.') {
				postfix += " ";
				do {
					postfix += src.charAt(i++);
				} while ((i < src.length()) && (Character.isDigit(src.charAt(i))));
				postfix += " ";
			} else {
				switch (op = src.charAt(i++)) {
				case '(':
					stk.push("(");
					break;
				case ')':
					while (stk.peek() != "(") {
						String tmp = stk.pop();
						postfix += tmp;
						if (tmp.length() == 1 && isOperator(tmp.charAt(0)))
							postfix += " ";
					}
					stk.pop();
					postfix += " ";
					break;
				case '+':
				case '-':
					while ((!stk.empty()) && (stk.peek() != "(")) {
						postfix += stk.pop() + " ";
					}
					stk.push(new Character(op).toString());
					break;
				case '*':
				case '/':
					while ((!stk.empty()) && ((stk.peek() == "*") || (stk.peek() == "/"))) {
						postfix += stk.pop() + " ";
					}
					stk.push(new Character(op).toString());
					break;
				}
			}
		}
		ListIterator<String> it = stk.listIterator(stk.size());
		while (it.hasPrevious())
			postfix += it.previous() + " ";
		return postfix.trim().replaceAll(" +\\.", ".");
	}

	public double getResult() {
		String postfix = getPostfix();
		Stack<String> stk = new Stack<String>();
		String parts[] = postfix.split(" +");
		double result = 0;
		for (int i = 0; i < parts.length; i++) {
			char tmp = parts[i].charAt(0);
			if (!isOperator(tmp)) {
				stk.push(parts[i]);
			} else {
				double a = Double.parseDouble(stk.pop());
				double b = Double.parseDouble(stk.pop());
				result = calculate(b, a, tmp);
				stk.push(String.valueOf(result));
			}
		}
		return result;
	}

	private boolean isOperator(char op) {
		return (op == '+' || op == '-' || op == '*' || op == '/');
	}
}