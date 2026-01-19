package com.vrn.backup.vaultdb.handler;

import com.vrn.backup.vaultdb.Core.model.BackupRequest;

import java.io.File;

public interface DatabaseHandler {
    /**
     * Returns the string identifier (e.g., "mysql")
     */
    String getType();

    /**
     * Performs the backup and returns the file (compressed).
     */
    File backup(BackupRequest request) throws Exception;

    /**
     * Restores the database from a given file.
     */
    void restore(BackupRequest request, File backupFile) throws Exception;
}

