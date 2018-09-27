package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-4-7
 */
public enum MessageType {
    text(1),
    audio(2),
    video(3),
    image(4),
    calendar(5),
    link(6),
    multilink(7),
    file(8),
    gps(9),
    vcard(10),
    emotion(11),
    event(12),
    custom(13),
    transmission(999),;

    private int typeId;

    MessageType(int typeId) {
        this.typeId = typeId;
    }

    public static MessageType getType(String typeStr) {
        try {
            return MessageType.valueOf(typeStr);
        } catch (Exception e) {
            return MessageType.text;
        }
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static void main(String[] args) {
        System.out.println(getType("event").name());
    }
}
