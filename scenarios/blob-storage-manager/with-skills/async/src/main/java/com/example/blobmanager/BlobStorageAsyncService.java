package com.example.blobmanager;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.specialized.BlobLeaseAsyncClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.Map;

/**
 * Asynchronous service wrapping common Azure Blob Storage operations.
 * Uses {@link BinaryData#fromFile(Path)} for memory-efficient large-file uploads
 * and lease-based concurrency control to prevent concurrent writers.
 */
public class BlobStorageAsyncService {

    private final BlobServiceAsyncClient serviceClient;

    public BlobStorageAsyncService(BlobServiceAsyncClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Uploads a file to a blob with optional metadata and index tags.
     * Uses {@link BinaryData#fromFile(Path)} so multi-gigabyte files are streamed
     * without being loaded entirely into memory.
     */
    public Mono<Void> upload(String containerName, String blobName, String filePath,
                             Map<String, String> metadata, Map<String, String> tags) {
        BlobContainerAsyncClient containerClient =
            serviceClient.getBlobContainerAsyncClient(containerName);

        return containerClient.createIfNotExists()
            .then(Mono.defer(() -> {
                BlobAsyncClient blobClient = containerClient.getBlobAsyncClient(blobName);

                ParallelTransferOptions transferOptions = new ParallelTransferOptions()
                    .setBlockSizeLong(4L * 1024 * 1024)
                    .setMaxConcurrency(8);

                BlobParallelUploadOptions options = new BlobParallelUploadOptions(
                    BinaryData.fromFile(Path.of(filePath)))
                    .setParallelTransferOptions(transferOptions);

                if (metadata != null && !metadata.isEmpty()) {
                    options.setMetadata(metadata);
                }
                if (tags != null && !tags.isEmpty()) {
                    options.setTags(tags);
                }

                return blobClient.uploadWithResponse(options).then();
            }));
    }

    /**
     * Downloads a blob to a local file, overwriting if it already exists.
     */
    public Mono<Void> download(String containerName, String blobName, String destinationPath) {
        BlobAsyncClient blobClient = serviceClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

        return blobClient.downloadToFile(destinationPath, true).then();
    }

    /**
     * Lists all blob names in the given container as a reactive stream.
     */
    public Flux<String> listBlobs(String containerName) {
        BlobContainerAsyncClient containerClient =
            serviceClient.getBlobContainerAsyncClient(containerName);

        return containerClient.listBlobs()
            .map(BlobItem::getName);
    }

    /**
     * Deletes a blob if it exists (no error if already absent).
     */
    public Mono<Void> delete(String containerName, String blobName) {
        BlobAsyncClient blobClient = serviceClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

        return blobClient.deleteIfExists().then();
    }

    /**
     * Acquires an exclusive lease on a blob to prevent concurrent writers.
     *
     * @return a Mono emitting the lease ID.
     */
    public Mono<String> acquireLease(String containerName, String blobName, int durationSeconds) {
        BlobAsyncClient blobClient = serviceClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

        BlobLeaseAsyncClient leaseClient = new BlobLeaseClientBuilder()
            .blobAsyncClient(blobClient)
            .buildAsyncClient();

        return leaseClient.acquireLease(durationSeconds);
    }

    /**
     * Releases a previously acquired lease.
     */
    public Mono<Void> releaseLease(String containerName, String blobName, String leaseId) {
        BlobAsyncClient blobClient = serviceClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

        BlobLeaseAsyncClient leaseClient = new BlobLeaseClientBuilder()
            .blobAsyncClient(blobClient)
            .leaseId(leaseId)
            .buildAsyncClient();

        return leaseClient.releaseLease().then();
    }

    /**
     * Uploads a file while holding a lease, preventing concurrent writers from
     * overwriting each other's changes.
     */
    public Mono<Void> uploadWithLease(String containerName, String blobName, String filePath,
                                      String leaseId, Map<String, String> metadata,
                                      Map<String, String> tags) {
        BlobAsyncClient blobClient = serviceClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

        BlobRequestConditions conditions = new BlobRequestConditions()
            .setLeaseId(leaseId);

        ParallelTransferOptions transferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(4L * 1024 * 1024)
            .setMaxConcurrency(8);

        BlobParallelUploadOptions options = new BlobParallelUploadOptions(
            BinaryData.fromFile(Path.of(filePath)))
            .setParallelTransferOptions(transferOptions)
            .setRequestConditions(conditions);

        if (metadata != null && !metadata.isEmpty()) {
            options.setMetadata(metadata);
        }
        if (tags != null && !tags.isEmpty()) {
            options.setTags(tags);
        }

        return blobClient.uploadWithResponse(options).then();
    }
}
