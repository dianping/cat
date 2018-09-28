package com.dianping.cat.configuration;

public enum ProblemLongType {

    LONG_CACHE("long-cache", 25) {
        @Override
        protected boolean checkLongType(String type) {
            return type.startsWith("Squirrel.") || type.startsWith("Cellar.") || type.startsWith("Cache.");
        }
    },

    LONG_CALL("long-call", 100) {
        @Override
        protected boolean checkLongType(String type) {
            return "PigeonCall".equals(type) || "OctoCall".equals(type) || "Call".equals(type);
        }
    },

    LONG_SERVICE("long-service", 100) {
        @Override
        protected boolean checkLongType(String type) {
            return "PigeonService".equals(type) || "OctoService".equals(type) || "Service".equals(type);
        }
    },

    LONG_SQL("long-sql", 100) {
        @Override
        protected boolean checkLongType(String type) {
            return "SQL".equals(type);
        }
    },

    LONG_URL("long-url", 1000) {
        @Override
        protected boolean checkLongType(String type) {
            return "URL".equals(type);
        }
    },

    LONG_MQ("long-mq", 100) {
        @Override
        protected boolean checkLongType(String type) {
            return "MtmqRecvMessage".equals(type) || "MafkaRecvMessage".equals(type);
        }
    };

    private String name;

    private int threshold;

    protected abstract boolean checkLongType(String type);

    public static ProblemLongType findByName(String name) {
        for (ProblemLongType longType : values()) {
            if (longType.getName().equals(name)) {
                return longType;
            }
        }

        throw new RuntimeException("Error long type " + name);
    }

    public static ProblemLongType findByMessageType(String type) {
        for (ProblemLongType longType : values()) {
            if (longType.checkLongType(type)) {
                return longType;
            }
        }

        return null;
    }

    ProblemLongType(String name, int threshold) {
        this.name = name;
        this.threshold = threshold;
    }

    public String getName() {
        return name;
    }

    public int getThreshold() {
        return threshold;
    }

}
