package com.vrn.backup.vaultdb.cli;

import com.vrn.backup.vaultdb.Core.exception.BackupException;
import com.vrn.backup.vaultdb.Core.model.BackupRequest;
import com.vrn.backup.vaultdb.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class BackupCommands {
    private static final Logger log = LoggerFactory.getLogger(BackupCommands.class);
    private final BackupService backupService;

    public BackupCommands(BackupService backupService) {
        this.backupService = backupService;
    }

    @ShellMethod(key = "backup create", value = "Create a database backup")
    public String createBackup(@ShellOption(help = "Database type (mysql, postgres, mongo)") String type,
                               @ShellOption(help = "Database Host") String host,
                               @ShellOption(defaultValue = "3306", help = "Database Port") int port,
                               @ShellOption(help = "Username") String user,
                               @ShellOption(help = "Password (or via env var)") String password,
                               @ShellOption(help = "Database Name") String dbName,
                               @ShellOption(defaultValue = "local", help = "Storage (local, s3)") String storage) {
        BackupRequest request = BackupRequest.builder()
                .type(type)
                .host(host)
                .port(port)
                .username(user)
                .password(password)
                .dbName(dbName)
                .storageType(storage)
                .build();

        try {
            backupService.performBackup(request);
            return "Backup completed successfully!";
        } catch (BackupException e) {
            log.error("Backup error", e);
            return "Backup failed: " + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return "Critical error: " + e.getMessage();
        }
    }

    @ShellMethod(key = "backup schedule", value = "Generate a cron entry for scheduling")
    public String scheduleHelp(@ShellOption(help = "Cron expression") String cron, @ShellOption(help = "Full command to run") String command) {
        // Since Java cannot easily modify system Crontab/TaskScheduler across all OSs reliably,
        // we generate the instruction for the user.
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Run this in PowerShell to schedule:\n" + "SchTasks /Create /SC DAILY /TN \"DBBackup\" /TR \"java -jar backup-cli.jar " + command + "\" /ST 02:00";
        } else {
            return "Add this to your crontab (crontab -e):\n" + cron + " java -jar /path/to/backup-cli.jar " + command;
        }
    }

    @ShellMethod(key = "backup restore", value = "Restore a database from a backup file")
    public String restoreBackup(@ShellOption(help = "Database type") String type,
                                @ShellOption(help = "Database Host") String host,
                                @ShellOption(defaultValue = "3306", help = "Database Port") int port,
                                @ShellOption(help = "Username") String user,
                                @ShellOption(help = "Password") String password,
                                @ShellOption(help = "Target Database Name") String dbName,
                                @ShellOption(help = "Path to backup file (remote key or local relative path)") String filePath) {
        BackupRequest request = BackupRequest.builder()
                .type(type).host(host).port(port).username(user).password(password).dbName(dbName)
                .build();
        try {
            backupService.performRestore(request, filePath);
            return "Restore process completed.";
        } catch (Exception e) {
            return "Restore failed: " + e.getMessage();
        }
    }

}
