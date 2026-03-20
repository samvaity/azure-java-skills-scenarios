package com.example.blobmanager;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo application that exercises every BlobStorageService operation:
 * upload with tags, list, download, lease-protected overwrite, and delete.
 */
public class Main {

    private static final String CONTAINER_NAME = "demo-container";
    private static final String BLOB_NAME = "sample-data.txt";
    private static final String UPLOAD_FILE = "sample-upload.txt";
    private static final String DOWNLOAD_FILE = "downloaded-sample.txt";

    public static void main(String[] args) throws IOException {
        System.out.println("=== Azure Blob Storage Manager — Sync Demo ===\n");

        // 1. Initialize configuration and service
        BlobStorageConfig config = new BlobStorageConfig();
        BlobServiceClient serviceClient = config.createServiceClient();
        BlobStorageService service = new BlobStorageService(serviceClient);
        System.out.println("[OK] Configuration and service initialized");
        System.out.println("     Endpoint: " + config.getEndpoint());

        // 2. Create a sample file to upload
        Path sampleFile = Path.of(UPLOAD_FILE);
        Files.writeString(sampleFile,
            "Hello, Azure Blob Storage! Sync demo.\nTimestamp: " + System.currentTimeMillis());
        System.out.println("[OK] Sample file created: " + sampleFile);

        // 3. Upload with metadata and blob index tags
        Map<String, String> metadata = new HashMap<>();
        metadata.put("author", "sync-demo");
        metadata.put("version", "1.0");

        Map<String, String> tags = new HashMap<>();
        tags.put("environment", "development");
        tags.put("project", "blob-manager");

        service.upload(CONTAINER_NAME, BLOB_NAME, sampleFile.toString(), metadata, tags);
        System.out.println("[OK] Uploaded blob '" + BLOB_NAME + "' with metadata and index tags");

        // 4. List blobs
        List<BlobItem> blobs = service.listBlobs(CONTAINER_NAME);
        System.out.println("[OK] Blobs in container '" + CONTAINER_NAME + "':");
        blobs.forEach(blob -> System.out.println("     - " + blob.getName()));

        // 5. Download
        service.download(CONTAINER_NAME, BLOB_NAME, DOWNLOAD_FILE);
        System.out.println("[OK] Downloaded blob to: " + DOWNLOAD_FILE);
        System.out.println("     Content: " + Files.readString(Path.of(DOWNLOAD_FILE)));

        // 6. Acquire a lease, overwrite the blob while holding the lease
        String leaseId = service.acquireLease(CONTAINER_NAME, BLOB_NAME, 30);
        System.out.println("[OK] Acquired lease: " + leaseId);

        Files.writeString(sampleFile,
            "Updated content via lease-protected overwrite.\nTimestamp: " + System.currentTimeMillis());
        service.uploadWithLease(CONTAINER_NAME, BLOB_NAME, sampleFile.toString(), leaseId);
        System.out.println("[OK] Overwrote blob with lease protection");

        service.releaseLease(CONTAINER_NAME, BLOB_NAME, leaseId);
        System.out.println("[OK] Released lease");

        // 7. Delete
        service.delete(CONTAINER_NAME, BLOB_NAME);
        System.out.println("[OK] Deleted blob: " + BLOB_NAME);

        // Cleanup local files
        Files.deleteIfExists(sampleFile);
        Files.deleteIfExists(Path.of(DOWNLOAD_FILE));
        System.out.println("\n=== Sync Demo Complete ===");
    }
}
