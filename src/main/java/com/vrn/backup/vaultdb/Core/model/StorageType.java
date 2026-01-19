package com.vrn.backup.vaultdb.Core.model;

public enum StorageType {
    LOCAL,
    S3;

    public static StorageType fromString(String text) {
        for (StorageType b : StorageType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return LOCAL;
    }
}