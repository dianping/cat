package com.dianping.cat.message.internal;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.io.BufReleaseHelper;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

public abstract class AbstractMessage implements Message {
    protected String type;
    private String name;
    private String status = SUCCESS;
    private int statusCode = 1;
    private long timestampInMillis;
    private CharSequence data;
    private boolean completed;

    AbstractMessage(String type, String name) {
        this.type = String.valueOf(type);
        this.name = String.valueOf(name);
        timestampInMillis = System.currentTimeMillis();
    }

    public void setData(String str) {
        data = str;
    }

    @Override
    public void addData(String keyValuePairs) {
        if (data == null) {
            data = keyValuePairs;
        } else if (data instanceof StringBuilder) {
            ((StringBuilder) data).append('&').append(keyValuePairs);
        } else {
            StringBuilder sb = new StringBuilder(data.length() + keyValuePairs.length() + 16);

            sb.append(data).append('&');
            sb.append(keyValuePairs);
            data = sb;
        }
    }

    @Override
    public void addData(String key, Object value) {
        if (data instanceof StringBuilder) {
            ((StringBuilder) data).append('&').append(key).append('=').append(value);
        } else {
            String str = String.valueOf(value);
            int old = data == null ? 0 : data.length();
            StringBuilder sb = new StringBuilder(old + key.length() + str.length() + 16);

            if (data != null) {
                sb.append(data).append('&');
            }

            sb.append(key).append('=').append(str);
            data = sb;
        }
    }

    @Override
    public CharSequence getData() {
        if (data == null) {
            return "";
        } else {
            return data;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public long getTimestamp() {
        return timestampInMillis;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean isSuccess() {
        return statusCode > 0;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;

        if (Message.SUCCESS.equals(this.status)) {
            statusCode = 1;
        } else {
            statusCode = -1;
        }
    }

    @Override
    public void setStatus(Throwable e) {
        status = e.getClass().getName();
        statusCode = -1;
    }

    @Override
    public void setSuccessStatus() {
        status = SUCCESS;
        statusCode = 1;
    }

    public void setTimestamp(long timestamp) {
        timestampInMillis = timestamp;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

        try {
            PlainTextMessageCodec codec = new PlainTextMessageCodec();

            codec.encodeMessage(this, buf);
            codec.reset();
            return buf.toString(Charset.forName("utf-8"));
        } finally {
            BufReleaseHelper.release(buf);
        }
    }

}
