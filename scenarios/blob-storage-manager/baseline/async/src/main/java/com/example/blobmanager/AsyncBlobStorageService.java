package com.example.blobmanager;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.specialized.BlobLeaseAsyncClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Asynchronous service wrapping Azure Blob Storage operations.
 * All methods return reactive types (Mono/Flux) for non-blocking I/O.
 * Handles large file uploads efficiently via parallel block transfers
 * and prevents concurrent overwrites using ETag-based optimistic concurrency.
 */
public class AsyncBlobStorageService {

    private static final long BLOCK_SIZE = 4L * 1024 * 1024;          // 4 MB blocks
    private static final int MAX_CONCURRENCY = 8;
    private static final long MAX_SINGLE_UPLOAD_SIZE = 256L * 1024 * 1024; // 256 MB

    private final BlobServiceAsyncClient serviceClient;
    private final ParallelTransferOptions parallelTransferOptions;

    public AsyncBlobStorageService(BlobServiceAsyncClient serviceClient) {
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
    public Mono<Void> upload(String containerName, String blobName, String filePath,
                             Map<String, String> metadata, Map<String, String> tags) {
        BlobContainerAsyncClient containerClient =
            serviceClient.getBlobContainerAsyncClient(containerName);

        return containerClient.createIfNotExists()
            .then(Mono.defer(() -> {
                BlobAsyncClient blobClient = containerClient.getBlobAsyncClient(blobName);

                return blobClient.exists().flatMap(exists -> {
                    BlobRequestConditions conditions = new BlobRequestConditions();

                    if (Boolean.TRUE.equals(exists)) {
                        return blobClient.getProperties()
                            .flatMap(props -> {
                                conditions.setIfMatch(props.getETag());
                                return performUpload(blobClient, filePath, metadata, tags, conditions);
                            });
                    } else {
                        conditions.setIfNoneMatch("*");
                        return performUpload(blobClient, filePath, metadata, tags, conditions);
                    }
                });
            }));
    }

    private Mono<Void> performUpload(BlobAsyncClient blobClient, String filePath,
                                     Map<String, String> metadata, Map<String, String> tags,
                                     BlobRequestConditions conditions) {
        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(filePath)
            .setParallelTransferOptions(parallelTransferOptions)
            .setMetadata(metadata)
            .setRequestConditions(conditions);

        Mono<Void> uploadMono = blobClient.uploadFromFileWithResponse(options).then();

        if (tags != null && !tags.isEmpty()) {
            return uploadMono.then(blobClient.setTags(tags));
        }
        return uploadMono;
    }

    /** Downloads a blob to a local file path. */
    public Mono<Void> download(String containerName, String blobName, String downloadPath) {
        BlobAsyncClient blobClient = serviceClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);
        return blobClient.downloadToFile(downloadPath, true).then();
    }

    /** Lists all blobs in the specified container. */
    public Flux<BlobItem> listBlobs(String containerName) {
        return serviceClient.getBlobContainerAsyncClient(containerName).listBlobs();
    }

    /** Deletes a blob if it exists. */
    public Mono<Void> delete(String containerName, String blobName) {
        BlobAsyncClient blobClient = serviceClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);
        return blobClient.deleteIfExists().then();
    }

    /** Acquires a lease on a blob (duration must be 15-60 seconds, or -1 for infinite). */
    public Mono<String> acquireLease(String containerName, String blobName, int durationSeconds) {
        BlobAsyncClient blobClient = serviceClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);
        BlobLeaseAsyncClient leaseClient = new BlobLeaseClientBuilder()
            .blobAsyncClient(blobClient)
            .buildAsyncClient();
        return leaseClient.acquireLease(durationSeconds);
    }

    /** Uploads a file while holding an active lease to prevent concurrent modifications. */
    public Mono<Void> uploadWithLease(String containerName, String blobName, String filePath,
                                      String leaseId) {
        BlobAsyncClient blobClient = serviceClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);
        BlobRequestConditions conditions = new BlobRequestConditions().setLeaseId(leaseId);

        BlobUploadFromFileOptions options = new BlobUploadFromFileOptions(filePath)
            .setParallelTransferOptions(parallelTransferOptions)
            .setRequestConditions(conditions);

        return blobClient.uploadFromFileWithResponse(options).then();
    }

    /** Releases a previously acquired lease. */
    public Mono<Void> releaseLease(String containerName, String blobName, String leaseId) {
        BlobAsyncClient blobClient = serviceClient.getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);
        BlobLeaseAsyncClient leaseClient = new BlobLeaseClientBuilder()
            .blobAsyncClient(blobClient)
            .leaseId(leaseId)
            .buildAsyncClient();
        return leaseClient.releaseLease();
    }
}
