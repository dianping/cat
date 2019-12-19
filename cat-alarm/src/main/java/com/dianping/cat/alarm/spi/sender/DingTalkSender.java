package com.dianping.cat.alarm.spi.sender;

import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DingTalkSender extends AbstractSender {

    private static final Gson g = new Gson();

    public static final String ID = AlertChannel.DINGDING.getName();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean send(SendMessageEntity message) {
        Sender sender = querySender();
//        boolean batchSend = sender.getBatchSend();
        boolean result = false;

        List<String> dingTalkTokens = message.getReceivers();

        for (String dingTalkToken : dingTalkTokens) {
            boolean success = sendDingTalk(message, dingTalkToken, sender);
            result = result || success;
        }

        return result;
    }

    /**
     * 钉钉机器人发送
     * @param message
     * @param dingTalkToken  可能包含备注
     * @param sender
     * @return
     */
    private boolean sendDingTalk(SendMessageEntity message, String dingTalkToken, Sender sender) {
        String domain = message.getGroup();
        String title = message.getTitle().replaceAll(",", " ");
        String content = message.getContent();

        String webHookURL = sender.getUrl();

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("msgtype", "text");
        JsonObject textJson = new JsonObject();
        content = "CAT报警：" + " - " + title + " \n " + content;
        textJson.addProperty("content", content);
        jsonBody.add("text", textJson);

        if (dingTalkToken.contains(":")) {
            dingTalkToken = dingTalkToken.split(":")[1];
        }

        return httpJsonPostSend(200, webHookURL + dingTalkToken, jsonBody.toString());
    }


    private boolean httpJsonPostSend(int successCode, String urlStr, String contentJsonStr) {
        URL url = null;
        HttpURLConnection conn = null;
        boolean sendSuccess = false;
        InputStream in = null;

        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            //转换为字节数组
            byte[] data = contentJsonStr.getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            conn.setRequestProperty("content-type", "application/json;charset=UTF-8");
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write(data);
            out.flush();
            out.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == successCode) {
                in = conn.getInputStream();
                String responseText = readBytes(in);
                JsonObject jsonResponse = g.fromJson(responseText, JsonObject.class);
                if (jsonResponse.has("errcode") && jsonResponse.get("errcode").getAsInt() == 0) {
                    sendSuccess = true;
                    return true;
                } else {
                    String errmsg = jsonResponse.has("errmsg") && jsonResponse.get("errmsg").getAsString().length() > 0 ? jsonResponse.get("errmsg").getAsString(): "ERROR";
                    throw new DingTalkResponseError(errmsg);
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            m_logger.error("Dingding send error: " + e.getMessage(), e);
            return false;
        } finally {
            close(in);
            close(conn);
            if (!sendSuccess) {
                recordSendLog(urlStr, contentJsonStr);
            }
        }
    }


    private static void close(HttpURLConnection closeable) {
        if (closeable != null) {
            try {
                closeable.disconnect();
            } catch (Exception e) {

            }
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {

            }
        }
    }

    private static String readBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int readCount = 0;
        while ((readCount = inputStream.read(buffer)) > -1) {
            out.write(buffer, 0, readCount);
        }
        return out.toString("UTF-8");
    }

    private static class DingTalkResponseError extends Exception {

        public DingTalkResponseError(String errMsg) {
            super(errMsg);
        }

    }

}
