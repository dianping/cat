package com.dianping.cat.configuration.property.transform;


import com.dianping.cat.configuration.property.Constants;
import com.dianping.cat.configuration.property.IVisitor;
import com.dianping.cat.configuration.property.IEntity;
import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;

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

    protected void endTag(String name) {
        level--;

        indent();
        sb.append("</").append(name).append(">\r\n");
    }

    protected String escape(Object value) {
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

    protected void indent() {
        if (!compact) {
            for (int i = level - 1; i >= 0; i--) {
                sb.append("   ");
            }
        }
    }

    protected void startTag(String name) {
        startTag(name, false, null);
    }

    protected void startTag(String name, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        startTag(name, null, closed, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
        startTag(name, null, false, dynamicAttributes, nameValues);
    }

    protected void startTag(String name, Object text, boolean closed, java.util.Map<String, String> dynamicAttributes, Object... nameValues) {
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
    public void visitProperty(Property property) {
        startTag(Constants.ENTITY_PROPERTY, true, null, Constants.ATTR_ID, property.getId(), Constants.ATTR_VALUE, property.getValue());
    }

    @Override
    public void visitPropertyConfig(PropertyConfig propertyConfig) {
        startTag(Constants.ENTITY_PROPERTY_CONFIG, null);

        if (!propertyConfig.getProperties().isEmpty()) {
            for (Property property : propertyConfig.getProperties().values().toArray(new Property[0])) {
                property.accept(visitor);
            }
        }

        endTag(Constants.ENTITY_PROPERTY_CONFIG);
    }
}
