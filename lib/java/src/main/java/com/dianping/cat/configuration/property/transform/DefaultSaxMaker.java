package com.dianping.cat.configuration.property.transform;

import com.dianping.cat.configuration.property.Constants;
import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;
import org.xml.sax.Attributes;

public class DefaultSaxMaker implements IMaker<Attributes> {

    @Override
    public Property buildProperty(Attributes attributes) {
        String id = attributes.getValue(Constants.ATTR_ID);
        String value = attributes.getValue(Constants.ATTR_VALUE);
        Property property = new Property(id);

        if (value != null) {
            property.setValue(value);
        }

        return property;
    }

    @Override
    public PropertyConfig buildPropertyConfig(Attributes attributes) {
        return new PropertyConfig();
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
