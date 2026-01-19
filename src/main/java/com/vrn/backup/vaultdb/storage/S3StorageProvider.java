package com.vrn.backup.vaultdb.storage;

import com.vrn.backup.vaultdb.Core.model.StorageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client; // Assume configured via @Bean in StorageConfig

    @Value("${storage.s3.bucket}")
    private String bucketName;

    public S3StorageProvider(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void upload(File file, String destinationPath) {
        PutObjectRequest req = PutObjectRequest.builder().bucket(bucketName).key(destinationPath + "/" + file.getName()).build();
        s3Client.putObject(req, file.toPath());
        System.out.println("Uploaded to S3: " + bucketName + "/" + destinationPath);
    }

    @Override
    public File download(String remotePath, String localTarget) {
        // S3 download logic
        return new File(localTarget);
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }
}
