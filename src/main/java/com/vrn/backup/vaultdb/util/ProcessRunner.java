package com.vrn.backup.vaultdb.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProcessRunner {

    // Overload for backward compatibility (used by backup methods)
    public int run(List<String> command, Map<String, String> envVars, File outputFile) throws Exception {
        return run(command, envVars, outputFile, null);
    }

    public int run(List<String> command, Map<String, String> envVars, File outputFile, File inputFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);

        if (envVars != null) {
            pb.environment().putAll(envVars);
        }

        // Redirect output to file directly
        if (outputFile != null) {
            pb.redirectOutput(outputFile);
        } else {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        // Handle Input Redirection (for Restore)
        if (inputFile != null) {
            pb.redirectInput(inputFile);
        }

        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Process failed with exit code: " + exitCode);
        }
        return exitCode;
    }
}
