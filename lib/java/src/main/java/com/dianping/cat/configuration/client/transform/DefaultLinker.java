package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultLinker implements ILinker {
    private boolean deferrable;
    private List<Runnable> deferedJobs = new ArrayList<Runnable>();

    public DefaultLinker(boolean deferrable) {
        this.deferrable = deferrable;
    }

    public void finish() {
        for (Runnable job : deferedJobs) {
            job.run();
        }
    }

    @Override
    public boolean onBind(final ClientConfig parent, final Bind bind) {
        parent.setBind(bind);
        return true;
    }

    @Override
    public boolean onDomain(final ClientConfig parent, final Domain domain) {
        if (deferrable) {
            deferedJobs.add(new Runnable() {
                @Override
                public void run() {
                    parent.addDomain(domain);
                }
            });
        } else {
            parent.addDomain(domain);
        }

        return true;
    }

    @Override
    public boolean onProperty(final ClientConfig parent, final Property property) {
        parent.addProperty(property);
        return true;
    }

    @Override
    public boolean onServer(final ClientConfig parent, final Server server) {
        parent.addServer(server);
        return true;
    }
}
