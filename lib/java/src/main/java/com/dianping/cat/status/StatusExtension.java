package com.dianping.cat.status;

import java.util.Map;

public interface StatusExtension {

    String getDescription();

    String getId();

    Map<String, String> getProperties();
}
