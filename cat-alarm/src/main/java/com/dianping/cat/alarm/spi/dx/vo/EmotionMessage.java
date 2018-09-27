package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class EmotionMessage implements XBody {
    private String category;
    private String type;
    private String name;

    public EmotionMessage() {
    }

    public EmotionMessage(String category, String type, String name) {
        this.category = category;
        this.type = type;
        this.name = name;
    }

    public String messageType() {
        return MessageType.emotion.name();
    }

    public boolean checkElementsNotNull() {
        return category != null && type != null && name != null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
