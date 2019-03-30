package org.hkijena.misa_imagej.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class ProcessUtils {

    private ProcessUtils() {

    }

    /**
     * Queries standard output with a timeout
     * @param executable
     * @param args
     * @return
     */
    public static String queryFast(Path executable, String ...args) {
        CommandLine commandLine = new CommandLine(executable.toFile());
        commandLine.addArguments(args);
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(5000);
        executor.setWatchdog(watchdog);

        // Capture stdout
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler outputStreamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(outputStreamHandler);

        try {
            int exitValue = executor.execute(commandLine);

            if(exitValue == 0) {
                return new String(outputStream.toByteArray());
            }
            else {
                return null;
            }

        } catch (IOException e) {
            return null;
        }
    }

    public static int executeFast(Path executable, String... args) {
        CommandLine commandLine = new CommandLine(executable.toFile());
        commandLine.addArguments(args);
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(5000);
        executor.setWatchdog(watchdog);

        try {
            return executor.execute(commandLine);
        } catch (IOException e) {
            return -1;
        }
    }

}
