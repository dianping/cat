package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class EventMessage implements XBody {

    private String type;
    private String text;

    public EventMessage() {
    }

    public EventMessage(String type, String text) {
        this.text = text;
        this.type = type;
    }

    public String messageType() {
        return MessageType.event.name();
    }

    public boolean checkElementsNotNull() {
        return type != null && text != null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
