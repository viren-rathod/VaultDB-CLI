package com.vrn.backup.vaultdb.handler;

import com.vrn.backup.vaultdb.Core.model.BackupRequest;
import com.vrn.backup.vaultdb.util.ProcessRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class MySqlHandler implements DatabaseHandler {
    private final ProcessRunner processRunner;

    public MySqlHandler(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    @Override
    public String getType() {
        return "mysql";
    }

    @Override
    public File backup(BackupRequest request) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("backup_%s_%s.sql", request.getDbName(), timestamp);
        File outputFile = new File(System.getProperty("java.io.tmpdir"), fileName);

        // Security: Pass password via ENV, not CLI args
        Map<String, String> env = Map.of("MYSQL_PWD", request.getPassword());

        List<String> command = List.of(
                "mysqldump",
                "-h", request.getHost(),
                "-P", String.valueOf(request.getPort()),
                "-u", request.getUsername(), "--single-transaction", // For InnoDB consistency without locking
                "--quick", // Don't buffer query results
                request.getDbName());

        int i = processRunner.run(command, env, outputFile); // In a real scenario, we would gzip this file here before returning
        return outputFile;
    }

    @Override
    public void restore(BackupRequest request, File backupFile) throws Exception {
        // Implementation for restore: mysql -u user -p db < file.sql
        Map<String, String> env = Map.of("MYSQL_PWD", request.getPassword());

        List<String> command = List.of(
                "mysql",
                "-h", request.getHost(),
                "-P", String.valueOf(request.getPort()),
                "-u", request.getUsername(),
                request.getDbName()
        );

        // Pass backupFile as 'inputFile' to piped into the process
        processRunner.run(command, env, null, backupFile);
    }
}
