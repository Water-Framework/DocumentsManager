package it.water.documents.manager.repository.s3.api;

import it.water.core.api.service.Service;

import java.io.InputStream;

/**
 * S3 Client interface for document storage operations.
 * Provides low-level operations for interacting with S3-compatible storage services.
 *
 * @author Christian Claudio Rosati
 */
public interface DocumentRepositoryS3Client extends Service {

    /**
     * Uploads content to S3 as a byte array.
     *
     * @param bucket  the target bucket name
     * @param key     the object key (path) where the content will be stored
     * @param content the content to upload as byte array
     */
    void upload(String bucket, String key, byte[] content);

    /**
     * Uploads content to S3 from an InputStream.
     *
     * @param bucket        the target bucket name
     * @param key           the object key (path) where the content will be stored
     * @param content       the content to upload as InputStream
     * @param contentLength the length of the content in bytes
     */
    void upload(String bucket, String key, InputStream content, long contentLength);

    /**
     * Downloads an object from S3 as a byte array.
     *
     * @param bucket the source bucket name
     * @param key    the object key (path) to download
     * @return the object content as byte array
     */
    byte[] download(String bucket, String key);

    /**
     * Downloads an object from S3 as an InputStream.
     * The caller is responsible for closing the returned InputStream.
     *
     * @param bucket the source bucket name
     * @param key    the object key (path) to download
     * @return the object content as InputStream
     */
    InputStream downloadAsStream(String bucket, String key);

    /**
     * Deletes an object from S3.
     *
     * @param bucket the bucket name containing the object
     * @param key    the object key (path) to delete
     */
    void delete(String bucket, String key);

    /**
     * Checks if an object exists in S3.
     *
     * @param bucket the bucket name to check
     * @param key    the object key (path) to check
     * @return true if the object exists, false otherwise
     */
    boolean exists(String bucket, String key);

    /**
     * Copies an object from source to destination within S3.
     * Can be used for move/rename operations in combination with delete.
     *
     * @param sourceBucket the source bucket name
     * @param sourceKey    the source object key (path)
     * @param destBucket   the destination bucket name
     * @param destKey      the destination object key (path)
     */
    void copy(String sourceBucket, String sourceKey, String destBucket, String destKey);

}