package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.Constants;
import com.dianping.cat.configuration.client.IEntity;
import com.dianping.cat.configuration.client.entity.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

public class DefaultSaxParser extends DefaultHandler {
    private DefaultLinker linker = new DefaultLinker(true);
    private DefaultSaxMaker maker = new DefaultSaxMaker();
    private Stack<String> tags = new Stack<String>();
    private Stack<Object> objects = new Stack<Object>();
    private IEntity<?> entity;
    private StringBuilder text = new StringBuilder();

    public static ClientConfig parse(InputSource is) throws SAXException, IOException {
        return parseEntity(ClientConfig.class, is);
    }

    public static ClientConfig parse(InputStream in) throws SAXException, IOException {
        return parse(new InputSource(in));
    }

    public static ClientConfig parse(Reader reader) throws SAXException, IOException {
        return parse(new InputSource(reader));
    }

    public static ClientConfig parse(String xml) throws SAXException, IOException {
        return parse(new InputSource(new StringReader(xml)));
    }

    public static <T extends IEntity<?>> T parseEntity(Class<T> type, String xml) throws SAXException, IOException {
        return parseEntity(type, new InputSource(new StringReader(xml)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends IEntity<?>> T parseEntity(Class<T> type, InputSource is) throws SAXException, IOException {
        try {
            DefaultSaxParser handler = new DefaultSaxParser();
            SAXParserFactory factory = SAXParserFactory.newInstance();

            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);

            factory.newSAXParser().parse(is, handler);
            return (T) handler.getEntity();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to get SAX parser instance!", e);
        }
    }


    @SuppressWarnings("unchecked")
    protected <T> T convert(Class<T> type, String value, T defaultValue) {
        if (value == null || value.length() == 0) {
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

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        text.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        linker.finish();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (uri == null || uri.length() == 0) {
            Object currentObj = objects.pop();

            tags.pop();

            if (currentObj instanceof Property) {
                Property property = (Property) currentObj;

                property.setText(getText());
            }
        }

        text.setLength(0);
    }

    private IEntity<?> getEntity() {
        return entity;
    }

    protected String getText() {
        return text.toString();
    }

    private void parseForBind(Bind parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForConfig(ClientConfig parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ENTITY_SERVERS.equals(qName) || Constants.ENTITY_PROPERTIES.equals(qName)) {
            objects.push(parentObj);
        } else if (Constants.ENTITY_SERVER.equals(qName)) {
            Server server = maker.buildServer(attributes);

            linker.onServer(parentObj, server);
            objects.push(server);
        } else if (Constants.ENTITY_DOMAIN.equals(qName)) {
            Domain domain = maker.buildDomain(attributes);

            linker.onDomain(parentObj, domain);
            objects.push(domain);
        } else if (Constants.ENTITY_BIND.equals(qName)) {
            Bind bind = maker.buildBind(attributes);

            linker.onBind(parentObj, bind);
            objects.push(bind);
        } else if (Constants.ENTITY_PROPERTY.equals(qName)) {
            Property property = maker.buildProperty(attributes);

            linker.onProperty(parentObj, property);
            objects.push(property);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under config!", qName));
        }

        tags.push(qName);
    }

    private void parseForDomain(Domain parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForProperty(Property parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForServer(Server parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseRoot(String qName, Attributes attributes) throws SAXException {
        if (Constants.ENTITY_CONFIG.equals(qName)) {
            ClientConfig config = maker.buildConfig(attributes);

            entity = config;
            objects.push(config);
            tags.push(qName);
        } else if (Constants.ENTITY_SERVER.equals(qName)) {
            Server server = maker.buildServer(attributes);

            entity = server;
            objects.push(server);
            tags.push(qName);
        } else if (Constants.ENTITY_DOMAIN.equals(qName)) {
            Domain domain = maker.buildDomain(attributes);

            entity = domain;
            objects.push(domain);
            tags.push(qName);
        } else if (Constants.ENTITY_BIND.equals(qName)) {
            Bind bind = maker.buildBind(attributes);

            entity = bind;
            objects.push(bind);
            tags.push(qName);
        } else if (Constants.ENTITY_PROPERTY.equals(qName)) {
            Property property = maker.buildProperty(attributes);

            entity = property;
            objects.push(property);
            tags.push(qName);
        } else {
            throw new SAXException("Unknown root element(" + qName + ") found!");
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (uri == null || uri.length() == 0) {
            if (objects.isEmpty()) { // root
                parseRoot(qName, attributes);
            } else {
                Object parent = objects.peek();
                String tag = tags.peek();

                if (parent instanceof ClientConfig) {
                    parseForConfig((ClientConfig) parent, tag, qName, attributes);
                } else if (parent instanceof Server) {
                    parseForServer((Server) parent, tag, qName, attributes);
                } else if (parent instanceof Domain) {
                    parseForDomain((Domain) parent, tag, qName, attributes);
                } else if (parent instanceof Bind) {
                    parseForBind((Bind) parent, tag, qName, attributes);
                } else if (parent instanceof Property) {
                    parseForProperty((Property) parent, tag, qName, attributes);
                } else {
                    throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
                }
            }

            text.setLength(0);
        } else {
            throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
        }
    }
}
