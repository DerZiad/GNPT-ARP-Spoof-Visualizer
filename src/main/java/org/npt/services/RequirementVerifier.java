package org.npt.services;

import lombok.extern.slf4j.Slf4j;
import org.npt.exception.ShutdownException;

import java.io.IOException;

@Slf4j
public class RequirementVerifier {

    private static final String VALID = "found";
    private static final String NOT_FOUND = "not found";

    public static void configureJavaFX() {

    }

    public static void validate() throws ShutdownException {
        boolean requirementSatisfied = true;
        boolean isLinux = isLinux();
        log.info("Checking whether running operating system is linux : {}", isLinux ? VALID : NOT_FOUND);
        requirementSatisfied &= isLinux;

        boolean isJava21 = isJava21();
        log.info("Checking whether the runtime is Java 21 : {}", isJava21 ? VALID : NOT_FOUND);
        requirementSatisfied &= isJava21;

        boolean isRoot = isRoot();
        log.info("Checking whether the current user is administrator : {}", isRoot ? VALID : NOT_FOUND);
        requirementSatisfied &= isRoot;

        boolean nmapExists = nmapExists();
        log.info("Checking whether nmap is available : {}", nmapExists ? VALID : NOT_FOUND);
        requirementSatisfied &= nmapExists;

        boolean dsniffAvailable = isDsniffAvailable();
        log.info("Checking whether dsniff is available : {}", dsniffAvailable ? VALID : NOT_FOUND);
        requirementSatisfied &= dsniffAvailable;

        if(!requirementSatisfied){
            throw new ShutdownException("Aborting !", ShutdownException.ShutdownExceptionErrorCode.UNSATISFIED_REQUIREMENTS);
        }
    }

    private static boolean isDsniffAvailable() {
        return commandExists("arpspoof");
    }

    private static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static boolean isRoot() {
        return System.getProperty("user.name").equals("root");
    }

    private static boolean isJava21() {
        return Runtime.version().feature() == 21;
    }

    private static boolean nmapExists() {
        return commandExists("nmap");
    }

    private static boolean commandExists(final String command) {
        try {
            Process p = new ProcessBuilder("which", command)
                    .redirectErrorStream(true)
                    .start();
            return p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
