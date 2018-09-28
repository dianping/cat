package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.IEntity;
import com.dianping.cat.configuration.client.IVisitor;
import com.dianping.cat.configuration.client.entity.*;

import java.util.Stack;

public class DefaultMerger implements IVisitor {

    private Stack<Object> objects = new Stack<Object>();

    private ClientConfig config;

    public DefaultMerger() {
    }

    public DefaultMerger(ClientConfig config) {
        this.config = config;
        objects.push(config);
    }

    public ClientConfig getConfig() {
        return config;
    }

    protected Stack<Object> getObjects() {
        return objects;
    }

    public <T> void merge(IEntity<T> to, IEntity<T> from) {
        objects.push(to);
        from.accept(this);
        objects.pop();
    }

    protected void mergeBind(Bind to, Bind from) {
        to.mergeAttributes(from);
    }

    protected void mergeConfig(ClientConfig to, ClientConfig from) {
        to.mergeAttributes(from);
    }

    protected void mergeDomain(Domain to, Domain from) {
        to.mergeAttributes(from);
    }

    protected void mergeProperty(Property to, Property from) {
        to.mergeAttributes(from);
        to.setText(from.getText());
    }

    protected void mergeServer(Server to, Server from) {
        to.mergeAttributes(from);
    }

    @Override
    public void visitBind(Bind from) {
        Bind to = (Bind) objects.peek();

        mergeBind(to, from);
        visitBindChildren(to, from);
    }

    protected void visitBindChildren(Bind to, Bind from) {
    }

    @Override
    public void visitConfig(ClientConfig from) {
        ClientConfig to = (ClientConfig) objects.peek();

        mergeConfig(to, from);
        visitConfigChildren(to, from);
    }

    protected void visitConfigChildren(ClientConfig to, ClientConfig from) {
        for (Server source : from.getServers()) {
            Server target = to.findServer(source.getIp());

            if (target == null) {
                target = new Server(source.getIp());
                to.addServer(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }

        for (Domain source : from.getDomains().values()) {
            Domain target = to.findDomain(source.getId());

            if (target == null) {
                target = new Domain(source.getId());
                to.addDomain(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }

        if (from.getBind() != null) {
            Bind target = to.getBind();

            if (target == null) {
                target = new Bind();
                to.setBind(target);
            }

            objects.push(target);
            from.getBind().accept(this);
            objects.pop();
        }

        for (Property source : from.getProperties()) {
            Property target = null;

            if (target == null) {
                target = new Property();
                to.addProperty(target);
            }

            objects.push(target);
            source.accept(this);
            objects.pop();
        }
    }

    @Override
    public void visitDomain(Domain from) {
        Domain to = (Domain) objects.peek();

        mergeDomain(to, from);
        visitDomainChildren(to, from);
    }

    protected void visitDomainChildren(Domain to, Domain from) {
    }

    @Override
    public void visitProperty(Property from) {
        Property to = (Property) objects.peek();

        mergeProperty(to, from);
        visitPropertyChildren(to, from);
    }

    protected void visitPropertyChildren(Property to, Property from) {
    }

    @Override
    public void visitServer(Server from) {
        Server to = (Server) objects.peek();

        mergeServer(to, from);
        visitServerChildren(to, from);
    }

    protected void visitServerChildren(Server to, Server from) {
    }
}
