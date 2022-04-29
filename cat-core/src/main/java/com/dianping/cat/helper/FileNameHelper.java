package com.dianping.cat.helper;

public class FileNameHelper {
    private static final String DATA_EXTENSION_TYPE = ".dat";
    private static final String IDX_EXTENSION_TYPE = ".idx";
    public static String getIdxNameByDataFile(String dataFilePath){
        if (dataFilePath.endsWith(DATA_EXTENSION_TYPE)) {
            return dataFilePath.substring(0,dataFilePath.length()-4)+IDX_EXTENSION_TYPE;
        }else {
            return dataFilePath+IDX_EXTENSION_TYPE;
        }
    }
}