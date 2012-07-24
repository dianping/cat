package com.site.initialization;

import java.util.List;

import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultModuleManager extends ContainerHolder implements ModuleManager {
	@Inject
	private String m_topLevelModules;

	@Override
	public Module[] getTopLevelModules() {
		if (m_topLevelModules != null) {
			List<String> hints = Splitters.by(',').trim().noEmptyItem().split(m_topLevelModules);
			Module[] topLevelModules = new Module[hints.size()];
			int index = 0;

			for (String hint : hints) {
				topLevelModules[index++] = lookup(Module.class, hint);
			}

			return topLevelModules;
		} else {
			return new Module[0];
		}
	}

	public void setTopLevelModules(String topLevelModules) {
		m_topLevelModules = topLevelModules;
	}
}
