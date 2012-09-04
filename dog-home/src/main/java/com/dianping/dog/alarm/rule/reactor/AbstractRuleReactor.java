package com.dianping.dog.alarm.rule.reactor;

import com.dianping.dog.alarm.rule.DefaultRuleContext;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleContext;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.site.lookup.annotation.Inject;

public abstract class AbstractRuleReactor<T extends Event> implements EventListener<T> {

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private Rule m_rule;

	protected abstract Event createNextEvent(RuleContext ctx);

	protected T getEvent(RuleContext ctx) {
		return ctx.getAttribute("event");
	}

	@Override
	public void onEvent(T event) {
		DefaultRuleContext ctx = new DefaultRuleContext();

		prepare(ctx, event);
		ctx.setAttribute("event", event);

		if (m_rule.apply(ctx)) {
			Event nextEvent = createNextEvent(ctx);

			if (nextEvent != null) {
				m_dispatcher.dispatch(nextEvent);
			}
		}
	}

	protected abstract void prepare(RuleContext ctx, T event);

	protected void setDispatcher(EventDispatcher dispatcher) {
		m_dispatcher = dispatcher;
	}

	protected void setRule(Rule rule) {
		m_rule = rule;
	}
}
