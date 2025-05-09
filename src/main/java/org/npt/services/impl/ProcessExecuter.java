package org.npt.services.impl;

import lombok.Getter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Getter
public class ProcessExecuter implements Runnable {

    private final String processName;

    private final String command;

    private final Instant instant = Instant.now();

    private final Boolean selfReadOfProcessContent;

    private final Process process = null;

    private ProcessExecuter(String command, String processName, Boolean readAndStore) {
        this.command = command;
        this.processName = processName;
        this.selfReadOfProcessContent = readAndStore;
    }

    public static ProcessExecuter execute(String processName, String[] command, Boolean selfReadOfProcessContent) {
        String commandString = String.join(" ", command);
        ProcessExecuter asynchronousProcessExecuter = new ProcessExecuter(commandString, processName, selfReadOfProcessContent);
        Thread thread = new Thread(asynchronousProcessExecuter);
        thread.start();
        return asynchronousProcessExecuter;
    }

    public void stop() {
        this.process.destroyForcibly();
    }

    @Override
    public void run() {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
