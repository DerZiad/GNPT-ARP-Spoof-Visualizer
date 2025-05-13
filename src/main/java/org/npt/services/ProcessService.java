package org.npt.services;

import org.npt.models.Task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public interface ProcessService {

    List<Task> tasks = new ArrayList<>();

    public static void execute(String processName, String[] command) {
        String commandString = String.join(" ", command);
        Task task = new Task(processName, commandString);
        tasks.add(task);
        Thread thread = new Thread(task);
        thread.start();
    }

    class ProcessUtils {

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
