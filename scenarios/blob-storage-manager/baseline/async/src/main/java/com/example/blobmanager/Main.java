package com.example.blobmanager;

import com.azure.storage.blob.BlobServiceAsyncClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Demo application that exercises every AsyncBlobStorageService operation
 * as a single reactive pipeline: upload with tags, list, download,
 * lease-protected overwrite, and delete.
 */
public class Main {

    private static final String CONTAINER_NAME = "demo-container";
    private static final String BLOB_NAME = "sample-data.txt";
    private static final String UPLOAD_FILE = "sample-upload.txt";
    private static final String DOWNLOAD_FILE = "downloaded-sample.txt";

    public static void main(String[] args) throws IOException {
        System.out.println("=== Azure Blob Storage Manager — Async Demo ===\n");

        // 1. Initialize configuration and service
        BlobStorageConfig config = new BlobStorageConfig();
        BlobServiceAsyncClient asyncClient = config.createAsyncServiceClient();
        AsyncBlobStorageService service = new AsyncBlobStorageService(asyncClient);
        System.out.println("[OK] Configuration and service initialized");
        System.out.println("     Endpoint: " + config.getEndpoint());

        // 2. Create a sample file to upload
        Path sampleFile = Path.of(UPLOAD_FILE);
        Files.writeString(sampleFile,
            "Hello, Azure Blob Storage! Async demo.\nTimestamp: " + System.currentTimeMillis());
        System.out.println("[OK] Sample file created: " + sampleFile);

        // 3. Prepare metadata and blob index tags
        Map<String, String> metadata = new HashMap<>();
        metadata.put("author", "async-demo");
        metadata.put("version", "1.0");

        Map<String, String> tags = new HashMap<>();
        tags.put("environment", "development");
        tags.put("project", "blob-manager");

        // 4. Build a single reactive pipeline for all operations
        Mono<Void> pipeline = service
            .upload(CONTAINER_NAME, BLOB_NAME, sampleFile.toString(), metadata, tags)
            .doOnSuccess(v -> System.out.println(
                "[OK] Uploaded blob '" + BLOB_NAME + "' with metadata and index tags"))

            // List blobs
            .thenMany(service.listBlobs(CONTAINER_NAME)
                .doOnSubscribe(s -> System.out.println(
                    "[OK] Blobs in container '" + CONTAINER_NAME + "':"))
                .doOnNext(blob -> System.out.println("     - " + blob.getName())))
            .then()

            // Download
            .then(service.download(CONTAINER_NAME, BLOB_NAME, DOWNLOAD_FILE))
            .doOnSuccess(v -> {
                System.out.println("[OK] Downloaded blob to: " + DOWNLOAD_FILE);
                try {
                    System.out.println("     Content: " + Files.readString(Path.of(DOWNLOAD_FILE)));
                } catch (IOException e) {
                    System.err.println("     Could not read downloaded file: " + e.getMessage());
                }
            })

            // Acquire lease, overwrite blob, release lease
            .then(service.acquireLease(CONTAINER_NAME, BLOB_NAME, 30))
            .doOnSuccess(id -> System.out.println("[OK] Acquired lease: " + id))
            .flatMap(leaseId -> {
                try {
                    Files.writeString(sampleFile,
                        "Updated content via lease-protected overwrite.\nTimestamp: "
                            + System.currentTimeMillis());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                return service.uploadWithLease(
                        CONTAINER_NAME, BLOB_NAME, sampleFile.toString(), leaseId)
                    .doOnSuccess(v -> System.out.println(
                        "[OK] Overwrote blob with lease protection"))
                    .then(service.releaseLease(CONTAINER_NAME, BLOB_NAME, leaseId))
                    .doOnSuccess(v -> System.out.println("[OK] Released lease"));
            })

            // Delete
            .then(service.delete(CONTAINER_NAME, BLOB_NAME))
            .doOnSuccess(v -> System.out.println("[OK] Deleted blob: " + BLOB_NAME));

        // 5. Execute the pipeline (blocks until complete)
        pipeline.block();

        // Cleanup local files
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(Path.of(DOWNLOAD_FILE));
        System.out.println("\n=== Async Demo Complete ===");
    }
}
