package com.dianping.cat.consumer.storage.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.storage.builder.StorageBuilder;

@Named
public class StorageBuilderManager extends ContainerHolder implements Initializable {

	private Map<String, StorageBuilder> m_storageBuilders;

	public List<String> getDefaultMethods(String type) {
		StorageBuilder storageBuilder = m_storageBuilders.get(type);

		if (storageBuilder != null) {
			return storageBuilder.getDefaultMethods();
		} else {
			return Collections.emptyList();
		}
	}

	public StorageBuilder getStorageBuilder(String type) {
		return m_storageBuilders.get(type);
	}

	@Override
	public void initialize() throws InitializationException {
		m_storageBuilders = lookupMap(StorageBuilder.class);
	}

}
