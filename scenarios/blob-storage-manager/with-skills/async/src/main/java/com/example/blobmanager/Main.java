package com.example.blobmanager;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.CredentialUnavailableException;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobStorageException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Demonstrates every blob operation using the asynchronous service:
 * upload → list → download → lease + overwrite → delete.
 * Operations are composed into a single reactive pipeline.
 */
public class Main {

    private static final String CONTAINER_NAME = "demo-container";
    private static final String BLOB_NAME = "sample-data.txt";

    public static void main(String[] args) {
        try {
            // --- Configuration ---
            BlobStorageConfig config = new BlobStorageConfig();
            BlobServiceAsyncClient serviceClient = config.createServiceClient();
            BlobStorageAsyncService service = new BlobStorageAsyncService(serviceClient);

            // --- Prepare sample file ---
            Path sampleFile = Files.createTempFile("blob-demo-", ".txt");
            Files.writeString(sampleFile, "Hello from Azure Blob Storage Manager (async)!");
            Path downloadFile = Path.of("downloaded-" + BLOB_NAME);

            Map<String, String> metadata = Map.of("author", "demo", "version", "1.0");
            Map<String, String> tags = Map.of("project", "blob-manager", "environment", "dev");

            // Build a reactive pipeline: upload → list → download → lease+overwrite → delete
            service.upload(CONTAINER_NAME, BLOB_NAME, sampleFile.toString(), metadata, tags)
                .doOnSuccess(v -> System.out.println("[async] Uploaded: " + BLOB_NAME))

                .then(service.listBlobs(CONTAINER_NAME).collectList())
                .doOnNext(blobs -> System.out.println("[async] Blobs in container: " + blobs))

                .then(service.download(CONTAINER_NAME, BLOB_NAME, downloadFile.toString()))
                .doOnSuccess(v -> {
                    try {
                        System.out.println("[async] Downloaded to: " + downloadFile);
                        System.out.println("[async] Content: " + Files.readString(downloadFile));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })

                .then(service.acquireLease(CONTAINER_NAME, BLOB_NAME, 30))
                .flatMap(leaseId -> {
                    System.out.println("[async] Acquired lease: " + leaseId);
                    try {
                        Files.writeString(sampleFile,
                            "Updated content with lease protection (async)");
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                    return service.uploadWithLease(CONTAINER_NAME, BLOB_NAME,
                            sampleFile.toString(), leaseId, metadata, tags)
                        .doOnSuccess(v ->
                            System.out.println("[async] Overwrote blob with lease protection"))
                        .then(service.releaseLease(CONTAINER_NAME, BLOB_NAME, leaseId))
                        .doOnSuccess(v -> System.out.println("[async] Released lease"));
                })

                .then(service.delete(CONTAINER_NAME, BLOB_NAME))
                .doOnSuccess(v -> System.out.println("[async] Deleted: " + BLOB_NAME))

                .block();

            // Cleanup temp files
            Files.deleteIfExists(sampleFile);
            Files.deleteIfExists(downloadFile);

            System.out.println("[async] Demo complete.");

        } catch (CredentialUnavailableException e) {
            System.err.println("No credential could authenticate: " + e.getMessage());
        } catch (ClientAuthenticationException e) {
            System.err.println("Authentication error: " + e.getMessage());
        } catch (BlobStorageException e) {
            System.err.println("Blob storage error (HTTP " + e.getStatusCode() + "): "
                + e.getErrorCode());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
