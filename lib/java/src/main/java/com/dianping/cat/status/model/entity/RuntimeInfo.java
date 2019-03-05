package com.dianping.cat.status.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

@Data
@EqualsAndHashCode(callSuper = true)
public class RuntimeInfo extends BaseEntity<RuntimeInfo> {
    private long startTime;
    private long upTime;
    private String javaVersion;
    private String userName;
    private String userDir;
    private String javaClasspath;

    public RuntimeInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitRuntime(this);
    }

    @Override
    public void mergeAttributes(RuntimeInfo other) {
        startTime = other.getStartTime();

        upTime = other.getUpTime();

        if (other.getJavaVersion() != null) {
            javaVersion = other.getJavaVersion();
        }

        if (other.getUserName() != null) {
            userName = other.getUserName();
        }
    }

    public RuntimeInfo setJavaClasspath(String javaClasspath) {
        this.javaClasspath = javaClasspath;
        return this;
    }

    public RuntimeInfo setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
        return this;
    }

    public RuntimeInfo setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public RuntimeInfo setUpTime(long upTime) {
        this.upTime = upTime;
        return this;
    }

    public RuntimeInfo setUserDir(String userDir) {
        this.userDir = userDir;
        return this;
    }

    public RuntimeInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

}
