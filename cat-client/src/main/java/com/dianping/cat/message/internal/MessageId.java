package com.dianping.cat.message.internal;

import org.unidal.helper.Splitters;

import java.util.List;

public class MessageId {
    private static final long VERSION1_THRESHOLD = 1325347200000L; // Jan. 1 2012

    private String m_domain;

    private String m_ipAddressInHex;

    private String m_pidInHex;

    private long m_timestamp;

    private int m_index;

    MessageId(String domain, String ipAddressInHex, String pidInHex, long timestamp, int index) {
        m_domain = domain;
        m_ipAddressInHex = ipAddressInHex;
        m_pidInHex = pidInHex;
        m_timestamp = timestamp;
        m_index = index;
    }

    public static MessageId parse(String messageId) {
        List<String> list = Splitters.by('-').split(messageId);
        int len = list.size();

        if (len >= 5) {
            String ipAddressInHex = list.get(len - 4);
            String pidInHex = list.get(len - 3);
            long timestamp = Long.parseLong(list.get(len - 2));
            int index = Integer.parseInt(list.get(len - 1));
            String domain;

            if (len > 5) { // allow domain contains '-'
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < len - 4; i++) {
                    if (i > 0) {
                        sb.append('-');
                    }

                    sb.append(list.get(i));
                }

                domain = sb.toString();
            } else {
                domain = list.get(0);
            }

            return new MessageId(domain, ipAddressInHex, pidInHex, timestamp, index);
        }

        throw new RuntimeException("Invalid message id format: " + messageId);
    }

    public String getDomain() {
        return m_domain;
    }

    public int getIndex() {
        return m_index;
    }

    public String getIpAddress() {
        StringBuilder sb = new StringBuilder();
        String local = m_ipAddressInHex;
        int length = local.length();

        try{
        for (int i = 0; i < length; i += 2) {
            char first = local.charAt(i);
            char next = local.charAt(i + 1);
            int temp = 0;

            if (first >= '0' && first <= '9') {
                temp += (first - '0') << 4;
            } else {
                temp += ((first - 'a') + 10) << 4;
            }
            if (next >= '0' && next <= '9') {
                temp += next - '0';
            } else {
                temp += (next - 'a') + 10;
            }

            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(temp);
        }}
        catch (Exception e){
            throw new RuntimeException("id: "+toString()+e.getMessage(),e);
        }

        return sb.toString();
    }

    public String getIpAddressInHex() {
        return m_ipAddressInHex;
    }

    public String getPid() {
        int length = m_pidInHex.length();
        int temp = 0;
        for (int i = 0; i < length; i += 1) {
            char c = m_pidInHex.charAt(i);

            temp=temp<<4;
            if (c >= '0' && c <= '9') {
                temp += (c - '0');
            } else {
                temp += ((c - 'a') + 10);
            }

        }

        return String.valueOf(temp);
    }

    public String getPidInHex() {
        return m_pidInHex;
    }

    public long getTimestamp() {
        if (m_timestamp > VERSION1_THRESHOLD) {
            return m_timestamp;
        } else {
            return m_timestamp * 3600 * 1000L;
        }
    }

    public int getVersion() {
        if (m_timestamp > VERSION1_THRESHOLD) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(m_domain.length() + 32);

        sb.append(m_domain);
        sb.append('-');
        sb.append(m_ipAddressInHex);
        sb.append('-');
        sb.append(m_pidInHex);
        sb.append('-');
        sb.append(m_timestamp);
        sb.append('-');
        sb.append(m_index);

        return sb.toString();
    }
}
