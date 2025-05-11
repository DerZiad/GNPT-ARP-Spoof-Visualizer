package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Task implements Runnable {

    private Process process;
    private final String processName;
    private final String command;

    public Task(String processName, String command){
        this.processName = processName;
        this.command = command;
    }

    @SneakyThrows
    @Override
    public void run() {
        process = Runtime.getRuntime().exec(command.split(" "));
    }

    public void destroy(){
        process.destroy();
    }
}
