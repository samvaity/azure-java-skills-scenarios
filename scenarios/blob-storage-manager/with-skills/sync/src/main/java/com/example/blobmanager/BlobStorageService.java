package com.example.blobmanager;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Synchronous service wrapping common Azure Blob Storage operations.
 * Uses parallel block upload for large files and lease-based concurrency control.
 */
public class BlobStorageService {

    private final BlobServiceClient serviceClient;

    public BlobStorageService(BlobServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Uploads a file to a blob with optional metadata and index tags.
     * Uses {@link BlobParallelUploadOptions} with streaming to handle multi-gigabyte
     * files without loading them entirely into memory.
     */
    public void upload(String containerName, String blobName, String filePath,
                       Map<String, String> metadata, Map<String, String> tags) {
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        containerClient.createIfNotExists();

        BlobClient blobClient = containerClient.getBlobClient(blobName);

        try (InputStream stream = new FileInputStream(filePath)) {
            ParallelTransferOptions transferOptions = new ParallelTransferOptions()
                .setBlockSizeLong(4L * 1024 * 1024)
                .setMaxConcurrency(8);

            BlobParallelUploadOptions options = new BlobParallelUploadOptions(stream)
                .setParallelTransferOptions(transferOptions);

            if (metadata != null && !metadata.isEmpty()) {
                options.setMetadata(metadata);
            }
            if (tags != null && !tags.isEmpty()) {
                options.setTags(tags);
            }

            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    /**
     * Downloads a blob to a local file, overwriting if it already exists.
     */
    public void download(String containerName, String blobName, String destinationPath) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        blobClient.downloadToFile(destinationPath, true);
    }

    /**
     * Lists all blob names in the given container.
     */
    public List<String> listBlobs(String containerName) {
        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
        return containerClient.listBlobs().stream()
            .map(BlobItem::getName)
            .toList();
    }

    /**
     * Deletes a blob if it exists (no error if already absent).
     */
    public void delete(String containerName, String blobName) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);
        blobClient.deleteIfExists();
    }

    /**
     * Acquires an exclusive lease on a blob to prevent concurrent writers.
     *
     * @return the lease ID required for lease-protected operations.
     */
    public String acquireLease(String containerName, String blobName, int durationSeconds) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);

        BlobLeaseClient leaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .buildClient();

        return leaseClient.acquireLease(durationSeconds);
    }

    /**
     * Releases a previously acquired lease.
     */
    public void releaseLease(String containerName, String blobName, String leaseId) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);

        BlobLeaseClient leaseClient = new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient();

        leaseClient.releaseLease();
    }

    /**
     * Uploads a file while holding a lease, preventing concurrent writers from
     * overwriting each other's changes. The request will fail if the lease is
     * not valid or held by another client.
     */
    public void uploadWithLease(String containerName, String blobName, String filePath,
                                String leaseId, Map<String, String> metadata,
                                Map<String, String> tags) {
        BlobClient blobClient = serviceClient.getBlobContainerClient(containerName)
            .getBlobClient(blobName);

        try (InputStream stream = new FileInputStream(filePath)) {
            BlobRequestConditions conditions = new BlobRequestConditions()
                .setLeaseId(leaseId);

            ParallelTransferOptions transferOptions = new ParallelTransferOptions()
                .setBlockSizeLong(4L * 1024 * 1024)
                .setMaxConcurrency(8);

            BlobParallelUploadOptions options = new BlobParallelUploadOptions(stream)
                .setParallelTransferOptions(transferOptions)
                .setRequestConditions(conditions);

            if (metadata != null && !metadata.isEmpty()) {
                options.setMetadata(metadata);
            }
            if (tags != null && !tags.isEmpty()) {
                options.setTags(tags);
            }

            blobClient.uploadWithResponse(options, null, Context.NONE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}
