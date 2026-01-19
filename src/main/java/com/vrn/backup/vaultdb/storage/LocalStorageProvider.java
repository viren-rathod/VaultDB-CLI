package com.vrn.backup.vaultdb.storage;

import com.vrn.backup.vaultdb.Core.model.StorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageProvider implements StorageProvider {

    @Value("${storage.local.dir:./backups}")
    private String baseDir;

    @Override
    public void upload(File file, String destinationPath) throws Exception {
        Path targetPath = Paths.get(baseDir, destinationPath, file.getName());
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("File saved locally to: " + targetPath.toAbsolutePath());
    }

    @Override
    public File download(String remotePath, String localTarget) throws Exception {
        File source = new File(baseDir, remotePath);
        File target = new File(localTarget);
        if (!source.exists()) {
            throw new RuntimeException("Local backup file not found: " + source.getAbsolutePath());
        }
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }
}