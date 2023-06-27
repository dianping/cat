package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class MultiLinkMessage implements XBody {
    private short num;
    private String content;

    public MultiLinkMessage() {
    }

    public MultiLinkMessage(short num, String content) {
        this.num = num;
        this.content = content;
    }

    public String messageType() {
        return MessageType.multilink.name();
    }

    public boolean checkElementsNotNull() {
        return content != null && num != 0;
    }

    public short getNum() {
        return num;
    }

    public void setNum(short num) {
        this.num = num;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
