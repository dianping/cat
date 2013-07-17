package com.dianping.cat.abtest.spi.interanl.conditions;

import java.util.List;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.abtest.model.entity.Condition;

public class ABTestConditionManager extends ContainerHolder {

	public boolean accept(List<Condition> conditions, HttpServletRequest request) {
		if (conditions == null || conditions.isEmpty()) {
			return true;
		}

		Stack<Boolean> operation = new Stack<Boolean>();
		Stack<String> operator = new Stack<String>();
		int size = conditions.size();

		for (int i = 0; i < size; i++) {
			Condition condition = conditions.get(i);
			AbstractABTestCondition filter = (AbstractABTestCondition) lookup(ABTestCondition.class, condition.getName());

			filter.setRequest(request);

			boolean isMarch = filter.accept(condition);
			operation.push(isMarch);

			if (i == 0) {
				operator.push(condition.getOperator());
				continue;
			} else if (i < size - 1) {
				String op = condition.getOperator();

				if (op.equalsIgnoreCase("and")) {
					operator.push(op);
				} else {// or
					if (operator.peek().equalsIgnoreCase("and")) {
						operator.pop();
						boolean result1 = operation.pop();
						boolean result2 = operation.pop();

						operation.push(result1 && result2);
					}

					operator.push("or");
				}
			} else if (i == size - 1) {
				while (!operator.isEmpty()) {
					String op = operator.pop();
					boolean result1 = operation.pop();
					boolean result2 = operation.pop();
					if (op.equalsIgnoreCase("and")) {
						operation.push(result1 && result2);
					} else {
						operation.push(result1 || result2);
					}
				}
			}
		}

		return operation.pop();
	}
}
