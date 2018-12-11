/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.log;

import com.dianping.cat.util.Properties;
import com.dianping.cat.util.Threads;

import java.io.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class CatLogger {
    private MessageFormat format;
    private BufferedWriter writer;
    private String lastPath;
    private boolean devMode;
    private ReentrantLock lock = new ReentrantLock();
    private static final String DEFAULT_BASE_DIR = "/data/applogs/cat";
    private static CatLogger LOGGER = new CatLogger();

    public static CatLogger getInstance() {
        return LOGGER;
    }

    private CatLogger() {
        String pattern = "[{0,date," + "MM-dd HH:mm:ss.SSS" + "}] [{1}] [{3}] {2}";
        String mode = Properties.forString().fromSystem().fromEnv().getProperty("devMode", "false");

        format = new MessageFormat(pattern);
        devMode = Boolean.parseBoolean(mode);
    }

    private void console(String message) {
        System.out.println(message);
    }

    public void error(String message) {
        out("ERROR", message, null);
    }

    public void error(String message, Throwable throwable) {
        out("ERROR", message, throwable);
    }

    private String formatMessage(String level, String message) {
        return format.format(new Object[]{new Date(), level, message, getCallerClassName()});
    }

    private String getCallerClassName() {
        String caller = Threads.getCallerClass();

        if (caller != null) {
            return caller;
        }

        StackTraceElement[] elements = new Exception().getStackTrace();

        if (elements.length > 1) {
            String className = elements[0].getClassName();

            int pos = className.lastIndexOf('$');

            if (pos < 0) {
                pos = className.lastIndexOf('.');
            }

            if (pos > 0) {
                return className.substring(pos + 1);
            } else {
                return className;
            }
        }

        return "N/A";
    }

    private File getFilePath(String path) throws IOException {
        File file = new File(path);
        String baseDir = Properties.forString().fromSystem().fromEnv().getProperty("CAT_HOME", DEFAULT_BASE_DIR);

        if (baseDir != null) {
            file = new File(baseDir, path);
        }

        return file.getCanonicalFile();
    }

    private BufferedWriter getWriter() throws IOException {
        MessageFormat logFileFormat = new MessageFormat("cat_client_{0,date,yyyyMMdd}.log");
        String path = logFileFormat.format(new Object[]{new Date()});

        if (!path.equals(lastPath)) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore it
                }
            }

            File file = getFilePath(path);
            file.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(file, true);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
            lastPath = path;
        }

        return writer;
    }

    public void info(String message) {
        out("INFO", message, null);
    }

    public void info(String message, Throwable throwable) {
        out("INFO", message, throwable);
    }

    private void out(String severity, String message, Throwable throwable) {
        lock.lock();

        try {
            String timedMessage = formatMessage(severity, message);

            if (devMode) {
                console(timedMessage);

                if (throwable != null) {
                    throwable.printStackTrace(System.out);
                }
            } else {
                try {
                    BufferedWriter writer = getWriter();

                    if (writer != null) {
                        writer.write(timedMessage);
                        writer.newLine();

                        if (throwable != null) {
                            throwable.printStackTrace(new PrintWriter(writer));
                        }

                        writer.flush();
                    }
                } catch (Exception e) {
                    console(formatMessage("ERROR", timedMessage + e.toString()));
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
