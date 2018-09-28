package com.dianping.cat.status.datasource.c3p0;

import lombok.Data;

@Data
public class C3P0MonitorInfo {
    private String jdbcUrl;
    private int numBusyConnections;
    private int numConnections;
    private int numIdleConnections;
    private long numFailedCheckOuts;
    private long numFailedCheckIns;
    private long numFailedIdleTests;

    @Override
    public String toString() {
        return "C3P0MonitorInfo [jdbcUrl=" + jdbcUrl + ", numBusyConnections=" + numBusyConnections
                + ", numConnections=" + numConnections + ", numIdleConnections=" + numIdleConnections
                + ", numFailedCheckOuts=" + numFailedCheckOuts + ", numFailedCheckIns=" + numFailedCheckIns
                + ", numFailedIdleTests=" + numFailedIdleTests + "]";
    }

}
