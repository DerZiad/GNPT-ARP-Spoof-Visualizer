package org.npt.beans.implementation;

import org.npt.exception.GatewayNotFoundException;
import org.npt.exception.ProcessFailureException;
import org.npt.models.Device;
import lombok.AccessLevel;
import lombok.Getter;
import org.npt.models.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter(AccessLevel.MODULE)
public class GatewayFinder {

    @Getter
    private Device gateway = null;

    private static GatewayFinder gatewayFinder = null;

    private GatewayFinder() throws ProcessFailureException, GatewayNotFoundException {
        scan();
    }

    public void scan() throws GatewayNotFoundException,ProcessFailureException {
        String command = "netstat -rn | grep -m 1 '^default\\|^0.0.0.0'";
        Process process;
        try {
            process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        } catch (IOException e) {
            throw new ProcessFailureException("Failed to start the process to find the gateway");
        }
        try {
            this.gateway = findGatewayInTheConsoleContent(process.getInputStream());
        }catch (IOException e){
            throw new ProcessFailureException("Failed to read the console content to find the gateway");
        }

    }

    public static GatewayFinder getInstance() throws ProcessFailureException, GatewayNotFoundException {
        if (gatewayFinder == null) {
            gatewayFinder = new GatewayFinder();
        }
        return gatewayFinder;
    }

    private Device findGatewayInTheConsoleContent(InputStream is) throws IOException, GatewayNotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Device gateway = null;
        String line;
        Pattern pattern = Pattern.compile("^default\\s+([\\d.]+)\\s+.*|^0.0.0.0\\s+([\\d.]+)");

        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
                gateway = new Device("Gateway", matcher.group(1) != null ? matcher.group(1) : matcher.group(2),0,0, Type.GATEWAY);
                break;
            }
        }

        reader.close();
        if (gateway != null) {
            return gateway;
        }
        throw new GatewayNotFoundException("Gateway could not be found");
    }

}
