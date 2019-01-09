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
package com.dianping.cat.status.jvm;

import java.lang.management.ThreadInfo;
import java.util.*;
import java.util.Map.Entry;

public class ThreadInfoWriter {
    private static final int MAX_FRAMES = 200;
    private static final String PROTOCOL_ID = "ThreadDumpCat";
    private Map<String, Integer> wordTable = new HashMap<String, Integer>();
    private short offset = 0;

    public ThreadInfoWriter() {
    }

    public String buildThreadsInfo(ThreadInfo[] threads) {
        StringBuilder result = new StringBuilder();
        StringBuilder codeResult = new StringBuilder();
        result.append(PROTOCOL_ID);
        result.append("1");

        int threadSize;
        for (ThreadInfo thread : threads) {
            threadSize = processThread(thread, codeResult);
            codeResult.insert(codeResult.length() - threadSize, String.format("%04d", threadSize));
        }

        appendWordTable(result);
        result.append(String.format("%08d", threads.length));
        result.append(codeResult);
        return result.toString();
    }

    private int processThread(ThreadInfo thread, StringBuilder codeResult) {
        int lastSize = codeResult.length();
        codeResult.append(String.format("%08d", thread.getThreadId()));
        String stateCode = getStateCode(thread.getThreadState());
        codeResult.append(stateCode);

        String name = thread.getThreadName();
        addToWordTable(name);
        String code = encode(name);
        codeResult.append(code);
        codeResult.append(buildExternalFlag(thread));

        name = thread.getLockName();
        if (name != null) {
            addToWordTable(name);
            code = encode(name);
            codeResult.append(code);
        }

        name = thread.getLockOwnerName();
        if (name != null) {
            addToWordTable(name);
            code = encode(name);
            codeResult.append(code);
            String ownerId = String.valueOf(thread.getLockOwnerId());
            codeResult.append(ownerId).append("#");
        }

        StackTraceElement[] stackTrace = thread.getStackTrace();
        byte stackSize = (byte) (stackTrace.length < MAX_FRAMES ? stackTrace.length : MAX_FRAMES);
        codeResult.append(String.format("%04d", stackSize));

        StackTraceElement stack;
        for (int i = 0; i < stackTrace.length && i < MAX_FRAMES; i++) {
            stack = stackTrace[i];
            processStack(stack, codeResult);
        }

        return codeResult.length() - lastSize;
    }

    private void processStack(StackTraceElement stack, StringBuilder codeResult) {
        String name = String.format("%s.%s", stack.getClassName(), stack.getMethodName());
        addToWordTable(name);
        String code = encode(name);
        codeResult.append(code);
        if (stack.getFileName() != null) {
            addToWordTable(stack.getFileName());
            code = encode(stack.getFileName());
            codeResult.append(code);

            int lineNumber = stack.getLineNumber();
            codeResult.append(String.format("%05d", lineNumber));
        } else {
            codeResult.append("0#");
            codeResult.append(String.format("%05d", 0));
        }
    }

    private void appendWordTable(StringBuilder result) {
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(wordTable.entrySet());

        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return (o1.getValue() - o2.getValue());
            }
        });

        result.append("[");
        for (Entry<String, Integer> word : list) {
            result.append(word.getKey()).append(",");
        }

        if (list.size() > 0) {
            result.deleteCharAt(result.length() - 1);
        }
        result.append("]");
    }

    private String buildExternalFlag(ThreadInfo thread) {
        byte externalFlag = 0;
        String result;

        if (thread.isInNative()) {
            externalFlag |= 1;
        }
        if (thread.isSuspended()) {
            externalFlag |= 2;
        }
        if (thread.getLockName() != null) {
            externalFlag |= 4;
        }
        if (thread.getLockOwnerName() != null) {
            externalFlag |= 8;
        }

        switch (externalFlag) {
            case 10:
                result = "A";
                break;
            case 11:
                result = "B";
                break;
            case 12:
                result = "C";
                break;
            case 13:
                result = "D";
                break;
            case 14:
                result = "E";
                break;
            case 15:
                result = "F";
                break;
            default:
                result = String.valueOf(externalFlag);
                break;
        }

        return result;
    }

    private String encode(String str) {
        int offset = wordTable.get(str);
        StringBuilder result = new StringBuilder(String.valueOf(offset));
        return result.append("#").toString();
    }

    private String getStateCode(Thread.State state) {
        switch (state) {
            case NEW:
                return "0";
            case RUNNABLE:
                return "1";
            case BLOCKED:
                return "2";
            case WAITING:
                return "3";
            case TIMED_WAITING:
                return "4";
            case TERMINATED:
                return "5";
            default:
                return "6";
        }
    }

    private void addToWordTable(String word) {
        if (!wordTable.containsKey(word)) {
            int code = offset;
            wordTable.put(word, code);
            offset++;
        }
    }
}
