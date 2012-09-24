package com.dianping.dog.notify.report;

import java.util.List;

import com.site.lookup.ContainerHolder;
import com.site.lookup.LookupException;

public class DefaultContainerHolder extends ContainerHolder {
	public <T> T lookup(Class<T> role) throws LookupException {
		return lookup(role, null);
	}

	public <T> T lookup(Class<T> role, String roleHint) throws LookupException {
		return super.lookup(role, roleHint);
	}

	public <T> List<T> lookupList(Class<T> role) throws LookupException {
		return super.lookupList(role);
	}

	public void release(Object component) throws LookupException {
		super.release(component);
	}
}
