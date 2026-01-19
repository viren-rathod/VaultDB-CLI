package com.vrn.backup.vaultdb.storage;

import com.vrn.backup.vaultdb.Core.model.StorageType;

import java.io.File;

public interface StorageProvider {
    void upload(File file, String destinationPath) throws Exception;

    File download(String remotePath, String localTarget) throws Exception;

    StorageType getStorageType();
}
