package com.dianping.cat.status;

import java.util.List;

/**
 * Created by yj.huang on 15-4-7.
 *
 * Collector heartbeat extension from user provided data
 */
public interface StatusExtensionBuilder {
    public StatusExtension collect();
}