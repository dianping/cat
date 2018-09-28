package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.Constants;
import com.dianping.cat.configuration.client.entity.*;
import org.xml.sax.Attributes;

public class DefaultSaxMaker implements IMaker<Attributes> {
    @Override
    public Bind buildBind(Attributes attributes) {
        String ip = attributes.getValue(Constants.ATTR_IP);
        String port = attributes.getValue(Constants.ATTR_PORT);
        Bind bind = new Bind();

        if (ip != null) {
            bind.setIp(ip);
        }

        if (port != null) {
            bind.setPort(convert(Integer.class, port, null));
        }

        return bind;
    }

    @Override
    public ClientConfig buildConfig(Attributes attributes) {
        String mode = attributes.getValue(Constants.ATTR_MODE);
        String enabled = attributes.getValue(Constants.ATTR_ENABLED);
        String dumpLocked = attributes.getValue(Constants.ATTR_DUMP_LOCKED);
        String domain = attributes.getValue(Constants.ATTR_DOMAIN);
        String maxMessageSize = attributes.getValue(Constants.ATTR_MAX_MESSAGE_SIZE);
        ClientConfig config = new ClientConfig(domain);

        if (mode != null) {
            config.setMode(mode);
        }

        if (enabled != null) {
            config.setEnabled(convert(Boolean.class, enabled, false));
        }

        if (dumpLocked != null) {
            config.setDumpLocked(convert(Boolean.class, dumpLocked, null));
        }

        if (maxMessageSize != null) {
            config.setMaxMessageSize(convert(Integer.class, maxMessageSize, 0));
        }

        return config;
    }

    @Override
    public Domain buildDomain(Attributes attributes) {
        String id = attributes.getValue(Constants.ATTR_ID);
        String ip = attributes.getValue(Constants.ATTR_IP);
        String enabled = attributes.getValue(Constants.ATTR_ENABLED);
        String maxMessageSize = attributes.getValue(Constants.ATTR_MAX_MESSAGE_SIZE);
        Domain domain = new Domain(id);

        if (ip != null) {
            domain.setIp(ip);
        }

        if (enabled != null) {
            domain.setEnabled(convert(Boolean.class, enabled, false));
        }

        if (maxMessageSize != null) {
            domain.setMaxMessageSize(convert(Integer.class, maxMessageSize, 0));
        }

        return domain;
    }

    @Override
    public Property buildProperty(Attributes attributes) {
        String name = attributes.getValue(Constants.ATTR_NAME);
        Property property = new Property();

        if (name != null) {
            property.setName(name);
        }

        return property;
    }

    @Override
    public Server buildServer(Attributes attributes) {
        String ip = attributes.getValue(Constants.ATTR_IP);
        String port = attributes.getValue(Constants.ATTR_PORT);
        String httpPort = attributes.getValue(Constants.ATTR_HTTP_PORT);
        String enabled = attributes.getValue(Constants.ATTR_ENABLED);
        Server server = new Server(ip);

        if (port != null) {
            server.setPort(convert(Integer.class, port, 0));
        }

        if (httpPort != null) {
            server.setHttpPort(convert(Integer.class, httpPort, 0));
        }

        if (enabled != null) {
            server.setEnabled(convert(Boolean.class, enabled, false));
        }

        return server;
    }

    @SuppressWarnings("unchecked")
    protected <T> T convert(Class<T> type, String value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (type == Boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(value);
        } else if (type == Long.class) {
            return (T) Long.valueOf(value);
        } else if (type == Short.class) {
            return (T) Short.valueOf(value);
        } else if (type == Float.class) {
            return (T) Float.valueOf(value);
        } else if (type == Double.class) {
            return (T) Double.valueOf(value);
        } else if (type == Byte.class) {
            return (T) Byte.valueOf(value);
        } else if (type == Character.class) {
            return (T) (Character) value.charAt(0);
        } else {
            return (T) value;
        }
    }
}
