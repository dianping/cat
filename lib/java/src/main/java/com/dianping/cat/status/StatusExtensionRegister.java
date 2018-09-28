package com.dianping.cat.status;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StatusExtensionRegister {

    private List<StatusExtension> extensions = new CopyOnWriteArrayList<StatusExtension>();
    private static final StatusExtensionRegister register = new StatusExtensionRegister();

    public static StatusExtensionRegister getInstance() {
        return register;
    }

    private StatusExtensionRegister() {
    }

    public List<StatusExtension> getStatusExtension() {
        return extensions;
    }

    public void register(StatusExtension extension) {
        extensions.add(extension);
    }

    public void unregister(StatusExtension extension) {
        extensions.remove(extension);
    }
}
