package com.vrn.backup.vaultdb.Core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackupRequest {
    private String type;
    private String host;
    private int port;
    private String username;
    private String password;
    private String dbName;
    private String storageType;
}