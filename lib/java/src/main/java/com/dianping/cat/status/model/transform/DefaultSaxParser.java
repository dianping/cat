package com.dianping.cat.status.model.transform;

import com.dianping.cat.status.model.Constants;
import com.dianping.cat.status.model.IEntity;
import com.dianping.cat.status.model.entity.*;
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

    public static StatusInfo parse(InputSource is) throws SAXException, IOException {
        return parseEntity(StatusInfo.class, is);
    }

    public static StatusInfo parse(InputStream in) throws SAXException, IOException {
        return parse(new InputSource(in));
    }

    public static StatusInfo parse(Reader reader) throws SAXException, IOException {
        return parse(new InputSource(reader));
    }

    public static StatusInfo parse(String xml) throws SAXException, IOException {
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
            String currentTag = tags.pop();

            if (currentObj instanceof RuntimeInfo) {
                RuntimeInfo runtime = (RuntimeInfo) currentObj;

                if (Constants.ELEMENT_USER_DIR.equals(currentTag)) {
                    runtime.setUserDir(getText());
                } else if (Constants.ELEMENT_JAVA_CLASSPATH.equals(currentTag)) {
                    runtime.setJavaClasspath(getText());
                }
            } else if (currentObj instanceof ThreadsInfo) {
                ThreadsInfo thread = (ThreadsInfo) currentObj;

                if (Constants.ELEMENT_DUMP.equals(currentTag)) {
                    thread.setDump(getText());
                }
            } else if (currentObj instanceof Extension) {
                Extension extension = (Extension) currentObj;

                if (Constants.ELEMENT_DESCRIPTION.equals(currentTag)) {
                    extension.setDescription(getText());
                }
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

    private void parseForCustomInfo(CustomInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForDisk(DiskInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ENTITY_DISK_VOLUME.equals(qName)) {
            DiskVolumeInfo diskVolume = maker.buildDiskVolume(attributes);

            linker.onDiskVolume(parentObj, diskVolume);
            objects.push(diskVolume);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under disk!", qName));
        }

        tags.push(qName);
    }

    private void parseForDiskVolume(DiskVolumeInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForExtension(Extension parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ELEMENT_DESCRIPTION.equals(qName)) {
            objects.push(parentObj);
        } else if (Constants.ENTITY_EXTENSIONDETAIL.equals(qName)) {
            ExtensionDetail extensionDetail = maker.buildExtensionDetail(attributes);

            linker.onExtensionDetail(parentObj, extensionDetail);
            objects.push(extensionDetail);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under extension!", qName));
        }

        tags.push(qName);
    }

    private void parseForExtensionDetail(ExtensionDetail parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForGc(GcInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForMemory(MemoryInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ENTITY_GC.equals(qName)) {
            GcInfo gc = maker.buildGc(attributes);

            linker.onGc(parentObj, gc);
            objects.push(gc);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under memory!", qName));
        }

        tags.push(qName);
    }

    private void parseForMessage(MessageInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForOs(OsInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        objects.push(parentObj);
        tags.push(qName);
    }

    private void parseForRuntime(RuntimeInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ELEMENT_USER_DIR.equals(qName) || Constants.ELEMENT_JAVA_CLASSPATH.equals(qName)) {
            objects.push(parentObj);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under runtime!", qName));
        }

        tags.push(qName);
    }

    private void parseForStatus(StatusInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ENTITY_RUNTIME.equals(qName)) {
            RuntimeInfo runtime = maker.buildRuntime(attributes);

            linker.onRuntime(parentObj, runtime);
            objects.push(runtime);
        } else if (Constants.ENTITY_OS.equals(qName)) {
            OsInfo os = maker.buildOs(attributes);

            linker.onOs(parentObj, os);
            objects.push(os);
        } else if (Constants.ENTITY_DISK.equals(qName)) {
            DiskInfo disk = maker.buildDisk(attributes);

            linker.onDisk(parentObj, disk);
            objects.push(disk);
        } else if (Constants.ENTITY_MEMORY.equals(qName)) {
            MemoryInfo memory = maker.buildMemory(attributes);

            linker.onMemory(parentObj, memory);
            objects.push(memory);
        } else if (Constants.ENTITY_THREAD.equals(qName)) {
            ThreadsInfo thread = maker.buildThread(attributes);

            linker.onThread(parentObj, thread);
            objects.push(thread);
        } else if (Constants.ENTITY_MESSAGE.equals(qName)) {
            MessageInfo message = maker.buildMessage(attributes);

            linker.onMessage(parentObj, message);
            objects.push(message);
        } else if (Constants.ENTITY_EXTENSION.equals(qName)) {
            Extension extension = maker.buildExtension(attributes);

            linker.onExtension(parentObj, extension);
            objects.push(extension);
        } else if (Constants.ENTITY_CUSTOMINFO.equals(qName)) {
            CustomInfo customInfo = maker.buildCustomInfo(attributes);

            linker.onCustomInfo(parentObj, customInfo);
            objects.push(customInfo);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under status!", qName));
        }

        tags.push(qName);
    }

    private void parseForThread(ThreadsInfo parentObj, String parentTag, String qName, Attributes attributes) throws SAXException {
        if (Constants.ELEMENT_DUMP.equals(qName)) {
            objects.push(parentObj);
        } else {
            throw new SAXException(String.format("Element(%s) is not expected under thread!", qName));
        }

        tags.push(qName);
    }

    private void parseRoot(String qName, Attributes attributes) throws SAXException {
        if (Constants.ENTITY_STATUS.equals(qName)) {
            StatusInfo status = maker.buildStatus(attributes);

            entity = status;
            objects.push(status);
            tags.push(qName);
        } else if (Constants.ENTITY_RUNTIME.equals(qName)) {
            RuntimeInfo runtime = maker.buildRuntime(attributes);

            entity = runtime;
            objects.push(runtime);
            tags.push(qName);
        } else if (Constants.ENTITY_OS.equals(qName)) {
            OsInfo os = maker.buildOs(attributes);

            entity = os;
            objects.push(os);
            tags.push(qName);
        } else if (Constants.ENTITY_MEMORY.equals(qName)) {
            MemoryInfo memory = maker.buildMemory(attributes);

            entity = memory;
            objects.push(memory);
            tags.push(qName);
        } else if (Constants.ENTITY_THREAD.equals(qName)) {
            ThreadsInfo thread = maker.buildThread(attributes);

            entity = thread;
            objects.push(thread);
            tags.push(qName);
        } else if (Constants.ENTITY_DISK.equals(qName)) {
            DiskInfo disk = maker.buildDisk(attributes);

            entity = disk;
            objects.push(disk);
            tags.push(qName);
        } else if (Constants.ENTITY_DISK_VOLUME.equals(qName)) {
            DiskVolumeInfo diskVolume = maker.buildDiskVolume(attributes);

            entity = diskVolume;
            objects.push(diskVolume);
            tags.push(qName);
        } else if (Constants.ENTITY_MESSAGE.equals(qName)) {
            MessageInfo message = maker.buildMessage(attributes);

            entity = message;
            objects.push(message);
            tags.push(qName);
        } else if (Constants.ENTITY_GC.equals(qName)) {
            GcInfo gc = maker.buildGc(attributes);

            entity = gc;
            objects.push(gc);
            tags.push(qName);
        } else if (Constants.ENTITY_EXTENSION.equals(qName)) {
            Extension extension = maker.buildExtension(attributes);

            entity = extension;
            objects.push(extension);
            tags.push(qName);
        } else if (Constants.ENTITY_EXTENSIONDETAIL.equals(qName)) {
            ExtensionDetail extensionDetail = maker.buildExtensionDetail(attributes);

            entity = extensionDetail;
            objects.push(extensionDetail);
            tags.push(qName);
        } else if (Constants.ENTITY_CUSTOMINFO.equals(qName)) {
            CustomInfo customInfo = maker.buildCustomInfo(attributes);

            entity = customInfo;
            objects.push(customInfo);
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

                if (parent instanceof StatusInfo) {
                    parseForStatus((StatusInfo) parent, tag, qName, attributes);
                } else if (parent instanceof RuntimeInfo) {
                    parseForRuntime((RuntimeInfo) parent, tag, qName, attributes);
                } else if (parent instanceof OsInfo) {
                    parseForOs((OsInfo) parent, tag, qName, attributes);
                } else if (parent instanceof MemoryInfo) {
                    parseForMemory((MemoryInfo) parent, tag, qName, attributes);
                } else if (parent instanceof ThreadsInfo) {
                    parseForThread((ThreadsInfo) parent, tag, qName, attributes);
                } else if (parent instanceof DiskInfo) {
                    parseForDisk((DiskInfo) parent, tag, qName, attributes);
                } else if (parent instanceof DiskVolumeInfo) {
                    parseForDiskVolume((DiskVolumeInfo) parent, tag, qName, attributes);
                } else if (parent instanceof MessageInfo) {
                    parseForMessage((MessageInfo) parent, tag, qName, attributes);
                } else if (parent instanceof GcInfo) {
                    parseForGc((GcInfo) parent, tag, qName, attributes);
                } else if (parent instanceof Extension) {
                    parseForExtension((Extension) parent, tag, qName, attributes);
                } else if (parent instanceof ExtensionDetail) {
                    parseForExtensionDetail((ExtensionDetail) parent, tag, qName, attributes);
                } else if (parent instanceof CustomInfo) {
                    parseForCustomInfo((CustomInfo) parent, tag, qName, attributes);
                } else {
                    throw new RuntimeException(String.format("Unknown entity(%s) under %s!", qName, parent.getClass().getName()));
                }
            }

            text.setLength(0);
        } else {
            throw new SAXException(String.format("Namespace(%s) is not supported by %s.", uri, this.getClass().getName()));
        }
    }

    protected java.util.Date toDate(String str, String format) {
        try {
            return new java.text.SimpleDateFormat(format).parse(str);
        } catch (java.text.ParseException e) {
            throw new RuntimeException(String.format("Unable to parse date(%s) in format(%s)!", str, format), e);
        }
    }
}
