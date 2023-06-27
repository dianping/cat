package com.dianping.cat.alarm.spi.dx.vo;

/**
 * @author weisenqiu
 * @version 1.0
 * @created 15-3-26
 */
public class CalendarMessage implements XBody {
    private long dtstart;
    private long dtend;
    private String summary;
    private String location;
    private String trigger;
    private String participant;
    private String remark;
    private long calendarID;

    public CalendarMessage() {
    }

    public CalendarMessage(long calendarID, long dtstart, long dtend, String summary,
                           String location, String trigger, String participant, String remark) {
        this.calendarID = calendarID;
        this.dtstart = dtstart;
        this.dtend = dtend;
        this.summary = summary;
        this.location = location;
        this.trigger = trigger;
        this.participant = participant;
        this.remark = remark;
    }

    public String messageType() {
        return MessageType.calendar.name();
    }

    public boolean checkElementsNotNull() {
        return dtstart != 0 && dtend != 0 && summary != null && location != null && trigger != null;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public long getDtstart() {
        return dtstart;
    }

    public void setDtstart(long dtstart) {
        this.dtstart = dtstart;
    }

    public long getDtend() {
        return dtend;
    }

    public void setDtend(long dtend) {
        this.dtend = dtend;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCalendarID() {
        return calendarID;
    }

    public void setCalendarID(long calendarID) {
        this.calendarID = calendarID;
    }
}
