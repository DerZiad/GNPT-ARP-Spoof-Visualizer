package org.npt.beans.implementation;

import org.npt.exception.FileException;
import org.npt.exception.ProcessFailureException;
import lombok.Getter;

import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProcessExecuter implements Runnable {

    private final String processName;

    private final String command;

    private final String logStorageFolder;

    private final Instant instant = Instant.now();

    private final Boolean selfReadOfProcessContent;

    private final Process process = null;

    private final List<Exception> exceptions = new ArrayList<>();

    private ProcessExecuter(String logStorageFolder, String command, String processName, Boolean readAndStore) {
        this.logStorageFolder = logStorageFolder;
        this.command = command;
        this.processName = processName;
        this.selfReadOfProcessContent = readAndStore;
    }

    public static ProcessExecuter execute(String processName, String logStorageFolder, String[] command, Boolean selfReadOfProcessContent) {
        String commandString = String.join(" ", command);
        ProcessExecuter asynchronousProcessExecuter = new ProcessExecuter(logStorageFolder, commandString, processName, selfReadOfProcessContent);
        Thread thread = new Thread(asynchronousProcessExecuter);
        thread.start();
        return asynchronousProcessExecuter;
    }


    private File createLogFile() throws IOException {
        File file = Paths.get(logStorageFolder, processName).toFile();
        file.createNewFile();
        return file;
    }

    public void stop(){
        this.process.destroyForcibly();
    }

    @Override
    public void run() {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            exceptions.add(new ProcessFailureException("Failed to start the process"));
            return;
        }
        if (!selfReadOfProcessContent) {
            File logFile;
            try {
                logFile = createLogFile();
            } catch (IOException e) {
                exceptions.add(new FileException("Failed to create log file"));
                return;
            }
            readAndWriteLogs(process.getInputStream(), process.getErrorStream(), logFile);

        }

    }

    private void readAndWriteLogs(InputStream processInputStream, InputStream errorStream, File logsFile) {
        try {
            // Create a single FileOutputStream for the log file to avoid reopening it
            FileOutputStream fileOutputStream = new FileOutputStream(logsFile, true);  // 'true' for appending

            // Thread for reading standard output
            new Thread(() -> {
                try (BufferedInputStream reader = new BufferedInputStream(processInputStream);
                     PrintWriter printWriter = new PrintWriter(fileOutputStream, true)) { // true for auto-flush
                    byte[] content = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = reader.read(content)) != -1) {
                        printWriter.write(new String(content, 0, bytesRead));
                        printWriter.flush();
                    }
                } catch (IOException e) {
                    exceptions.add(new FileException("Failed to write standard output to the file"));
                }
            }).start();

            // Thread for reading error output and writing it to the same file
            new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                     PrintWriter printWriter = new PrintWriter(fileOutputStream, true)) { // true for auto-flush
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        printWriter.println(line);  // Log each line from stderr
                        printWriter.flush();
                    }
                } catch (IOException e) {
                    exceptions.add(new FileException("Failed to write error output to the file"));
                }
            }).start();

        } catch (FileNotFoundException e) {
            exceptions.add(new FileException("Log file not found or could not be created"));
        }
    }

    static class ProcessUtils {
        public static String generateProcessNameFrom(String input){
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
