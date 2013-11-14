package com.dianping.cat.configuration;

import java.util.Stack;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.transform.DefaultMerger;

public class ClientConfigMerger extends DefaultMerger {
	public ClientConfigMerger(ClientConfig config) {
		super(config);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		if (domain.getIp() != null) {
			old.setIp(domain.getIp());
		}

		if (domain.getEnabled() != null) {
			old.setEnabled(domain.getEnabled());
		}

		if (domain.getMaxMessageSize() > 0) {
			old.setMaxMessageSize(domain.getMaxMessageSize());
		}
	}

	@Override
	protected void visitConfigChildren(ClientConfig to, ClientConfig from) {
		if (to != null) {
			Stack<Object> objs = getObjects();

			// if servers is configured, then override it instead of merge
			if (!from.getServers().isEmpty()) {
				to.getServers().clear();
				to.getServers().addAll(from.getServers());
			}

			// only configured domain in client configure will be merged
			for (Domain source : from.getDomains().values()) {
				Domain target = to.findDomain(source.getId());

				if (target == null) {
					target = new Domain(source.getId());
					to.addDomain(target);
				}

				if (to.getDomains().containsKey(source.getId())) {
					objs.push(target);
					source.accept(this);
					objs.pop();
				}
			}

			for (Property source : from.getProperties().values()) {
				Property target = to.findProperty(source.getName());

				if (target == null) {
					target = new Property(source.getName());
					to.addProperty(target);
				}

				objs.push(target);
				source.accept(this);
				objs.pop();
			}
		}
	}
}
