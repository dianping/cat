package com.dianping.cat.configuration;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.configuration.model.transform.DefaultMerger;

public class ClientConfigMerger extends DefaultMerger {
	public ClientConfigMerger(Config config) {
		super(config);
	}

	@Override
	protected void visitConfigChildren(Config old, Config config) {
		if (old != null) {
			getStack().push(old);

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

			getStack().pop();
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
	}
}
