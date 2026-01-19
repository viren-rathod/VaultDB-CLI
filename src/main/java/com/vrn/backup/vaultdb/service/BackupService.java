package com.vrn.backup.vaultdb.service;

import com.vrn.backup.vaultdb.Core.exception.BackupException;
import com.vrn.backup.vaultdb.Core.model.BackupRequest;
import com.vrn.backup.vaultdb.Core.model.StorageType;
import com.vrn.backup.vaultdb.handler.DatabaseHandler;
import com.vrn.backup.vaultdb.storage.StorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BackupService {
    private final Map<String, DatabaseHandler> handlers;
    private final Map<StorageType, StorageProvider> storageProviders;
    private final NotificationService notificationService;

    // Spring automatically injects all implementations of DatabaseHandler into a List
    public BackupService(
            List<DatabaseHandler> handlerList,
            List<StorageProvider> providerList,
            NotificationService notificationService
    ) {
        this.handlers = handlerList.stream().collect(Collectors.toMap(DatabaseHandler::getType, Function.identity()));
        this.storageProviders = providerList.stream().collect(Collectors.toMap(StorageProvider::getStorageType, Function.identity()));
        this.notificationService = notificationService;
    }


    public void performBackup(BackupRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Starting backup for DB: {}", request.getDbName());

        DatabaseHandler handler = handlers.get(request.getType().toLowerCase());
        if (handler == null) {
            throw new BackupException("No handler found for DB type: " + request.getType());
        }

        StorageType storageType = StorageType.fromString(request.getStorageType());
        StorageProvider provider = storageProviders.get(storageType);

        if (provider == null) {
            throw new BackupException("Storage provider not available/configured for: " + storageType);
        }

        try {
            File backupFile = handler.backup(request);

            // Upload to Storage
            provider.upload(backupFile, "backups/" + request.getDbName());

            // Cleanup local temp file
            // backupFile.delete();

            long duration = System.currentTimeMillis() - startTime;
            notificationService.sendSlackNotification("Backup Success: " + request.getDbName() + " (" + duration + "ms)");
        } catch (Exception e) {
            log.error("Backup failed", e);
            notificationService.sendSlackNotification("Backup FAILED: " + request.getDbName() + " - " + e.getMessage());
            if (e instanceof BackupException) throw (BackupException) e;
            throw new BackupException("Backup process failed", e);
        }
    }

    public void performRestore(BackupRequest request, String backupFilePath) {
        log.info("Starting restore for DB: {}", request.getDbName());
        DatabaseHandler handler = handlers.get(request.getType().toLowerCase());

        if (handler == null) {
            throw new IllegalArgumentException("No handler found for DB type: " + request.getType());
        }

        StorageType storageType = StorageType.fromString(request.getStorageType());
        StorageProvider provider = storageProviders.get(storageType);

        if (provider == null) {
            throw new BackupException("Storage provider not available/configured for: " + storageType);
        }

        try {
            // Ideally, the 'backupFilePath' is the relative path stored in the cloud/local dir
            File backupFile = provider.download(backupFilePath, System.getProperty("java.io.tmpdir") + "/restore_temp.sql");

            handler.restore(request, backupFile);

            notificationService.sendSlackNotification("Restore Success: " + request.getDbName());
        } catch (Exception e) {
            log.error("Restore failed", e);
            if (e instanceof BackupException) throw (BackupException) e;
            throw new BackupException("Restore process failed", e);
        }
    }
}
