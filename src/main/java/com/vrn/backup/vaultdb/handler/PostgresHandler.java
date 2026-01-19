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
public class PostgresHandler implements DatabaseHandler {
    private final ProcessRunner processRunner;

    public PostgresHandler(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    @Override
    public String getType() {
        return "postgres";
    }

    @Override
    public File backup(BackupRequest request) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("pg_backup_%s_%s.sql", request.getDbName(), timestamp);
        File outputFile = new File(System.getProperty("java.io.tmpdir"), fileName);

        // Postgres uses PGPASSWORD env var
        Map<String, String> env = Map.of("PGPASSWORD", request.getPassword());

        List<String> command = List.of(
                "pg_dump",
                "-h", request.getHost(),
                "-p", String.valueOf(request.getPort()),
                "-U", request.getUsername(),
                "-F", "p", // Plain text format
                "-f", outputFile.getAbsolutePath(),
                request.getDbName());

        int i = processRunner.run(command, env, null); // pg_dump writes to file via -f flag, so output redirection isn't strictly needed
        return outputFile;
    }

    @Override
    public void restore(BackupRequest request, File backupFile) throws Exception {
        // Implementation: psql -U user -d db -f file.sql
        Map<String, String> env = Map.of("PGPASSWORD", request.getPassword());

        List<String> command = List.of(
                "psql",
                "-h", request.getHost(),
                "-p", String.valueOf(request.getPort()),
                "-U", request.getUsername(),
                "-d", request.getDbName(),
                "-f", backupFile.getAbsolutePath()
        );
        int i = processRunner.run(command, env, null);
    }
}