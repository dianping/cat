package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15/8/26
 */
public class CustomMessage implements XBody {

    private String templateName;
    private String contentTitle;
    private String content;
    private String linkName;
    private String link;

    public CustomMessage() {
    }

    public CustomMessage(String templateName, String contentTitle, String content, String linkName, String link) {
        this.templateName = templateName;
        this.contentTitle = contentTitle;
        this.content = content;
        this.linkName = linkName;
        this.link = link;
    }

    public String messageType() {
        return MessageType.custom.name();
    }

    public boolean checkElementsNotNull() {
        return templateName != null && contentTitle != null && content != null;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
