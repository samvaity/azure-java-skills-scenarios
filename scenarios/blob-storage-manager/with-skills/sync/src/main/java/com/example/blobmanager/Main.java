package com.example.blobmanager;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.CredentialUnavailableException;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates every blob operation using the synchronous service:
 * upload → list → download → lease + overwrite → delete.
 */
public class Main {

    private static final String CONTAINER_NAME = "demo-container";
    private static final String BLOB_NAME = "sample-data.txt";

    public static void main(String[] args) {
        try {
            // --- Configuration ---
            BlobStorageConfig config = new BlobStorageConfig();
            BlobServiceClient serviceClient = config.createServiceClient();
            BlobStorageService service = new BlobStorageService(serviceClient);

            // --- Prepare sample file ---
            Path sampleFile = Files.createTempFile("blob-demo-", ".txt");
            Files.writeString(sampleFile, "Hello from Azure Blob Storage Manager (sync)!");
            Path downloadFile = Path.of("downloaded-" + BLOB_NAME);

            Map<String, String> metadata = Map.of("author", "demo", "version", "1.0");
            Map<String, String> tags = Map.of("project", "blob-manager", "environment", "dev");

            // 1. Upload with metadata and index tags
            service.upload(CONTAINER_NAME, BLOB_NAME, sampleFile.toString(), metadata, tags);
            System.out.println("[sync] Uploaded: " + BLOB_NAME);

            // 2. List blobs in the container
            List<String> blobs = service.listBlobs(CONTAINER_NAME);
            System.out.println("[sync] Blobs in container: " + blobs);

            // 3. Download the blob
            service.download(CONTAINER_NAME, BLOB_NAME, downloadFile.toString());
            System.out.println("[sync] Downloaded to: " + downloadFile);
            System.out.println("[sync] Content: " + Files.readString(downloadFile));

            // 4. Acquire a lease and overwrite with lease protection
            String leaseId = service.acquireLease(CONTAINER_NAME, BLOB_NAME, 30);
            System.out.println("[sync] Acquired lease: " + leaseId);

            Files.writeString(sampleFile, "Updated content with lease protection (sync)");
            service.uploadWithLease(CONTAINER_NAME, BLOB_NAME, sampleFile.toString(),
                leaseId, metadata, tags);
            System.out.println("[sync] Overwrote blob with lease protection");

            service.releaseLease(CONTAINER_NAME, BLOB_NAME, leaseId);
            System.out.println("[sync] Released lease");

            // 5. Delete the blob
            service.delete(CONTAINER_NAME, BLOB_NAME);
            System.out.println("[sync] Deleted: " + BLOB_NAME);

            // Cleanup temp files
            Files.deleteIfExists(sampleFile);
            Files.deleteIfExists(downloadFile);

            System.out.println("[sync] Demo complete.");

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
