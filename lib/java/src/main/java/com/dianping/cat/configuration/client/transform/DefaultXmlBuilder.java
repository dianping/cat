package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.Constants;
import com.dianping.cat.configuration.client.IEntity;
import com.dianping.cat.configuration.client.IVisitor;
import com.dianping.cat.configuration.client.entity.*;

public class DefaultXmlBuilder implements IVisitor {
    private IVisitor visitor = this;
    private int level;
    private StringBuilder sb;
    private boolean compact;

    public DefaultXmlBuilder() {
        this(false);
    }

    public DefaultXmlBuilder(boolean compact) {
        this(compact, new StringBuilder(4096));
    }

    public DefaultXmlBuilder(boolean compact, StringBuilder sb) {
        this.compact = compact;
        this.sb = sb;
        this.sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
    }

    public String buildXml(IEntity<?> entity) {
        entity.accept(visitor);
        return sb.toString();
    }

    private void endTag(String name) {
        level--;

        indent();
        sb.append("</").append(name).append(">\r\n");
    }

    private String escape(Object value) {
        return escape(value, false);
    }

    protected String escape(Object value, boolean text) {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        int len = str.length();
        StringBuilder sb = new StringBuilder(len + 16);

        for (int i = 0; i < len; i++) {
            final char ch = str.charAt(i);

            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    if (!text) {
                        sb.append("&quot;");
                        break;
                    }
                default:
                    sb.append(ch);
                    break;
            }
        }

        return sb.toString();
    }

    private void indent() {
        if (!compact) {
            for (int i = level - 1; i >= 0; i--) {
                sb.append("   ");
            }
        }
    }

    private void startTag(String name) {
        startTag(name, false, null);
    }

    private void startTag(String name, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        startTag(name, null, closed, dynamicAttributes, nameValues);
    }

    private void startTag(String name, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        startTag(name, null, false, dynamicAttributes, nameValues);
    }

    private void startTag(String name, Object text, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        indent();

        sb.append('<').append(name);

        int len = nameValues.length;

        for (int i = 0; i + 1 < len; i += 2) {
            Object attrName = nameValues[i];
            Object attrValue = nameValues[i + 1];

            if (attrValue != null) {
                sb.append(' ').append(attrName).append("=\"").append(escape(attrValue)).append('"');
            }
        }

        if (dynamicAttributes != null) {
            for (java.util.Map.Entry<String, String> e : dynamicAttributes.entrySet()) {
                sb.append(' ').append(e.getKey()).append("=\"").append(escape(e.getValue())).append('"');
            }
        }

        if (text != null && closed) {
            sb.append('>');
            sb.append(escape(text, true));
            sb.append("</").append(name).append(">\r\n");
        } else {
            if (closed) {
                sb.append('/');
            } else {
                level++;
            }

            sb.append(">\r\n");
        }
    }

    @Override
    public void visitBind(Bind bind) {
        startTag(Constants.ENTITY_BIND, true, null, Constants.ATTR_IP, bind.getIp(), Constants.ATTR_PORT, bind.getPort());
    }

    @Override
    public void visitConfig(ClientConfig config) {
        startTag(Constants.ENTITY_CONFIG, null, Constants.ATTR_MODE, config.getMode(), Constants.ATTR_ENABLED, config.isEnabled(), Constants.ATTR_DUMP_LOCKED, config.getDumpLocked(), Constants.ATTR_DOMAIN, config.getDomain(), Constants.ATTR_MAX_MESSAGE_SIZE, config.getMaxMessageSize());

        if (!config.getServers().isEmpty()) {
            startTag(Constants.ENTITY_SERVERS);

            for (Server server : config.getServers().toArray(new Server[0])) {
                server.accept(visitor);
            }

            endTag(Constants.ENTITY_SERVERS);
        }

        if (!config.getDomains().isEmpty()) {
            for (Domain domain : config.getDomains().values().toArray(new Domain[0])) {
                domain.accept(visitor);
            }
        }

        if (config.getBind() != null) {
            config.getBind().accept(visitor);
        }

        if (!config.getProperties().isEmpty()) {
            startTag(Constants.ENTITY_PROPERTIES);

            for (Property property : config.getProperties().toArray(new Property[0])) {
                property.accept(visitor);
            }

            endTag(Constants.ENTITY_PROPERTIES);
        }

        endTag(Constants.ENTITY_CONFIG);
    }

    @Override
    public void visitDomain(Domain domain) {
        startTag(Constants.ENTITY_DOMAIN, true, null, Constants.ATTR_ID, domain.getId(), Constants.ATTR_IP, domain.getIp(), Constants.ATTR_ENABLED, domain.isEnabled(), Constants.ATTR_MAX_MESSAGE_SIZE, domain.getMaxMessageSize());
    }

    @Override
    public void visitProperty(Property property) {
        startTag(Constants.ENTITY_PROPERTY, property.getText(), true, null, Constants.ATTR_NAME, property.getName());
    }

    @Override
    public void visitServer(Server server) {
        startTag(Constants.ENTITY_SERVER, true, null, Constants.ATTR_IP, server.getIp(), Constants.ATTR_PORT, server.getPort(), Constants.ATTR_HTTP_PORT, server.getHttpPort(), Constants.ATTR_ENABLED, server.isEnabled());
    }
}
