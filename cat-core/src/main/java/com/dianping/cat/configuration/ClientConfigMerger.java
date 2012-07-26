package com.dianping.cat.configuration;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.transform.DefaultMerger;

public class ClientConfigMerger extends DefaultMerger {
	public ClientConfigMerger(ClientConfig config) {
		super(config);
	}

	@Override
	protected void visitConfigChildren(ClientConfig old, ClientConfig config) {
		if (old != null) {
			getObjects().push(old);

			// if servers is configured, then override it instead of merge
			if (!config.getServers().isEmpty()) {
				old.getServers().clear();
				old.getServers().addAll(config.getServers());
			}

			// only configured domain in client configure will be merged
			for (Domain domain : config.getDomains().values()) {
				if (old.getDomains().containsKey(domain.getId())) {
					visitDomain(domain);
				}
			}

			for (Property property : config.getProperties().values()) {
				visitProperty(property);
			}

			getObjects().pop();
		}
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		if (domain.getIp() != null) {
			old.setIp(domain.getIp());
		}

		if (domain.getEnabled() != null) {
			old.setEnabled(domain.getEnabled());
		}

		if (domain.getMaxThreads() > 0) {
			old.setMaxThreads(domain.getMaxThreads());
		}
	}
}
