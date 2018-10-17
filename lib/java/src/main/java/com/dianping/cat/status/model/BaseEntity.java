package com.dianping.cat.status.model;


import com.dianping.cat.status.model.transform.DefaultXmlBuilder;

import java.util.Formattable;
import java.util.Formatter;

public abstract class BaseEntity<T> implements IEntity<T>, Formattable {

    protected void assertAttributeEquals(Object instance, String entityName, String name, Object expectedValue, Object actualValue) {
        if (expectedValue == null && actualValue != null || expectedValue != null && !expectedValue.equals(actualValue)) {
            throw new IllegalArgumentException(String.format("Mismatched entity(%s) found! Same %s attribute is expected! %s: %s.", entityName, name, entityName, instance));
        }
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean compact = (precision == 0);
        DefaultXmlBuilder builder = new DefaultXmlBuilder(compact);

        formatter.format(builder.buildXml(this));
    }

    @Override
    public String toString() {
        return new DefaultXmlBuilder().buildXml(this);
    }
}
