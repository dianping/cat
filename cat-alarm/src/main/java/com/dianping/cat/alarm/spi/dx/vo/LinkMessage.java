package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class LinkMessage implements XBody {
    private String title;
    private String image;
    private String content;
    private String link;

    public LinkMessage() {
    }

    public LinkMessage(String title, String image, String content, String link) {
        this.title = title;
        this.image = image;
        this.content = content;
        this.link = link;
    }

    public String messageType() {
        return MessageType.link.name();
    }

    public boolean checkElementsNotNull() {
        return title != null && image != null && content != null && link != null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
