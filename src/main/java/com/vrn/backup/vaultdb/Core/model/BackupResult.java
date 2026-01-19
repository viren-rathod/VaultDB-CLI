package com.vrn.backup.vaultdb.Core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupResult {
    private boolean success;
    private String message;
    private String backupPath;
    private LocalDateTime timestamp;
    private long durationMs;
}