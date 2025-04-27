package org.npt.beans.implementation;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class LogStorageCreator {

    public static String LOG_FOLDER_NAME = "LOGS%s";

    public static String createLogStorageFolder(){
        File file = new File(String.format(LOG_FOLDER_NAME,createRandomInstantString()));
        file.mkdirs();
        return file.getAbsolutePath();
    }

    private static String createRandomInstantString(){
        Random random = new Random();
        Date date = new Date(System.currentTimeMillis());
        String payload = "%s%s";
        StringBuilder content = new StringBuilder();
        for(int i = 0;i<10;i++){
            content.append(random.nextInt());
        }
        return String.format(payload,date,content);
    }
}
