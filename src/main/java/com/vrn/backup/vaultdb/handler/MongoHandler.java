package com.vrn.backup.vaultdb.handler;

import com.vrn.backup.vaultdb.Core.model.BackupRequest;
import com.vrn.backup.vaultdb.util.ProcessRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class MongoHandler implements DatabaseHandler {

    private final ProcessRunner processRunner;

    public MongoHandler(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    @Override
    public String getType() {
        return "mongo";
    }

    @Override
    public File backup(BackupRequest request) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("mongo_backup_%s_%s.archive", request.getDbName(), timestamp);
        File outputFile = new File(System.getProperty("java.io.tmpdir"), fileName);

        /**
         * Passwords are passed via arguments here as standard env var support varies.
         * --archive without a value writes to stdout, which ProcessRunner captures to
         * outputFile.
         */
        List<String> command = List.of(
                "mongodump",
                "--host", request.getHost(),
                "--port", String.valueOf(request.getPort()),
                "--username", request.getUsername(),
                "--password", request.getPassword(),
                "--db", request.getDbName(),
                "--archive",
                "--gzip");

        processRunner.run(command, null, outputFile);
        return outputFile;
    }

    @Override
    public void restore(BackupRequest request, File backupFile) throws Exception {
        /**
         * Reads from stdin (provided by backupFile via ProcessRunner) due to --archive
         */
        List<String> command = List.of(
                "mongorestore",
                "--host", request.getHost(),
                "--port", String.valueOf(request.getPort()),
                "--username", request.getUsername(),
                "--password", request.getPassword(),
                "--gzip",
                "--archive",
                "--drop");

        processRunner.run(command, null, null, backupFile);
    }
}