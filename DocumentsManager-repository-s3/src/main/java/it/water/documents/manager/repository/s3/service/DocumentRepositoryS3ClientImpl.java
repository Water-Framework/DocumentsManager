package it.water.documents.manager.repository.s3.service;

import it.water.core.interceptors.annotations.FrameworkComponent;
import it.water.core.interceptors.annotations.Inject;
import it.water.documents.manager.repository.s3.api.DocumentRepositoryS3Option;
import it.water.documents.manager.repository.s3.api.DocumentRepositoryS3Client;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;

@FrameworkComponent(services =  {DocumentRepositoryS3Client.class})
@Slf4j
public class DocumentRepositoryS3ClientImpl implements DocumentRepositoryS3Client {


    private S3Client s3Client;

    @Inject
    @Setter
    private DocumentRepositoryS3Option documentRepositoryOption;


    private S3Client getS3Client() {
        if (s3Client == null) {
            log.debug("Initializing S3 Client with endpoint: {}", documentRepositoryOption.getEndpoint());
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(documentRepositoryOption.getEndpoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(
                                    documentRepositoryOption.getAccessKey(),
                                    documentRepositoryOption.getSecretKey())))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(documentRepositoryOption.isPathStyleEnabled())
                            .build())
                    .region(Region.of(documentRepositoryOption.getRegion()))
                    .build();
            log.debug("S3 Client initialized successfully");
        }

        return s3Client;
    }


    @Override
    public void upload(String bucket, String key, byte[] content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        getS3Client().putObject(request, RequestBody.fromBytes(content));
    }

    @Override
    public void upload(String bucket, String key, InputStream content, long contentLength) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength(contentLength)
                .build();
        getS3Client().putObject(request, RequestBody.fromInputStream(content, contentLength));
    }

    @Override
    public byte[] download(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return getS3Client().getObjectAsBytes(request).asByteArray();
    }

    @Override
    public InputStream downloadAsStream(String bucket, String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return getS3Client().getObject(request);
    }

    @Override
    public void delete(String bucket, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        getS3Client().deleteObject(request);
    }

    @Override
    public boolean exists(String bucket, String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            getS3Client().headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void copy(String sourceBucket, String sourceKey, String destBucket, String destKey) {
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(destBucket)
                .destinationKey(destKey)
                .build();
        getS3Client().copyObject(request);
    }

}


