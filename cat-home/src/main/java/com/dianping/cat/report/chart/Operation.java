package com.dianping.cat.report.chart;

import java.util.ListIterator;
import java.util.Stack;

public class Operation {

	private String src;

	public Operation(String src) {
		this.src = src;
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
}