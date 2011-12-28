package com.dianping.cat;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.dianping.cat.message.MessageProducer;

/**
 * This is the main entry point to the system.
 * 
 * @author Frankie Wu
 */
public class Cat {
	private static PlexusContainer s_container;

	static {
		try {
			s_container = new DefaultPlexusContainer();
		} catch (PlexusContainerException e) {
			e.printStackTrace();
		}
	}

	public static MessageProducer getProducer() {
		try {
			return (MessageProducer) s_container.lookup(MessageProducer.class);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get instance of MessageProducer, "
			      + "please make sure the environment wa setup correctly!", e);
		}
	}
}
