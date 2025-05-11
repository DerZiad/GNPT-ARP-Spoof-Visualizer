package org.npt.services.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.npt.models.Task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessService {

    @Getter
    private static final List<Task> tasks = new ArrayList<>();

    private static ProcessService processService = null;

    public static void execute(String processName, String[] command) {
        String commandString = String.join(" ", command);
        Task task = new Task(processName, commandString);
        tasks.add(task);
        Thread thread = new Thread(task);
        thread.start();
    }

    public static void init(){
        if(processService == null)
            processService = new ProcessService();
    }

    public static class ProcessUtils {

        public static String generateProcessNameFrom(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(input.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 algorithm not found", e);
            }
        }
    }
}
