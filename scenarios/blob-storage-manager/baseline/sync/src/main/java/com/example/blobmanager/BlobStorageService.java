package com.example.blobmanager;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Synchronous service wrapping Azure Blob Storage operations.
 * Handles large file uploads efficiently via parallel block transfers
 * and prevents concurrent overwrites using ETag-based optimistic concurrency.
 */
public class BlobStorageService {

    private static final long BLOCK_SIZE = 4L * 1024 * 1024;          // 4 MB blocks
    private static final int MAX_CONCURRENCY = 8;
    private static final long MAX_SINGLE_UPLOAD_SIZE = 256L * 1024 * 1024; // 256 MB

    private final BlobServiceClient serviceClient;
    private final ParallelTransferOptions parallelTransferOptions;

    public BlobStorageService(BlobServiceClient serviceClient) {
        this.serviceClient = serviceClient;
        this.parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(BLOCK_SIZE)
            .setMaxConcurrency(MAX_CONCURRENCY)
            .setMaxSingleUploadSizeLong(MAX_SINGLE_UPLOAD_SIZE);
    }

    /**
     * Uploads a file to blob storage with optional metadata and index tags.
     * Uses parallel block upload for large files and ETag-based optimistic
     * concurrency to prevent silent overwrites by concurrent writers.
     */
    public void upload(String containerName, String blobName, String filePath,
                       Map<String, String> metadata, Map<String, String> tags) {
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        containerClient.createIfNotExists();

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // Optimistic concurrency control
        BlobRequestConditions conditions = new BlobRequestConditions();
        if (blobClient.exists()) {
            String etag = blobClient.getProperties().getETag();
            conditions.setIfMatch(etag);
        } else {
            conditions.setIfNoneMatch("*");
        }

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(filePath)
            .setParallelTransferOptions(parallelTransferOptions)
            .setMetadata(metadata)
            .setRequestConditions(conditions);

        blobClient.uploadFromFileWithResponse(options, null, null);

        // Set blob index tags for later querying
        if (tags != null && !tags.isEmpty()) {
            blobClient.setTags(tags);
        }
    }

    /** Downloads a blob to a local file path. */
    public void download(String containerName, String blobName, String downloadPath) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        blobClient.downloadToFile(downloadPath, true);
    }

    /** Lists all blobs in the specified container. */
    public List<BlobItem> listBlobs(String containerName) {
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        List<BlobItem> blobs = new ArrayList<>();
        containerClient.listBlobs().forEach(blobs::add);
        return blobs;
    }

    /** Deletes a blob if it exists. */
    public void delete(String containerName, String blobName) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        blobClient.deleteIfExists();
    }

    /** Acquires a lease on a blob (duration must be 15-60 seconds, or -1 for infinite). */
    public String acquireLease(String containerName, String blobName, int durationSeconds) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        BlobLeaseClient leaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .buildClient();
        return leaseClient.acquireLease(durationSeconds);
    }

    /** Uploads a file while holding an active lease to prevent concurrent modifications. */
    public void uploadWithLease(String containerName, String blobName, String filePath,
                                String leaseId) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        BlobRequestConditions conditions = new BlobRequestConditions().setLeaseId(leaseId);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(filePath)
            .setParallelTransferOptions(parallelTransferOptions)
            .setRequestConditions(conditions);

        blobClient.uploadFromFileWithResponse(options, null, null);
    }

    /** Releases a previously acquired lease. */
    public void releaseLease(String containerName, String blobName, String leaseId) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        BlobLeaseClient leaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();
        leaseClient.releaseLease();
    }
}
