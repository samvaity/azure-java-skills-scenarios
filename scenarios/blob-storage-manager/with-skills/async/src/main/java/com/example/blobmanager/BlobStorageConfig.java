package com.example.blobmanager;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

/**
 * Configuration for Azure Blob Storage using managed identity authentication.
 * All settings are read from environment variables — no secrets are hardcoded.
 */
public class BlobStorageConfig {

    private static final String ENDPOINT_ENV_VAR = "AZURE_STORAGE_ENDPOINT";
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 800;
    private static final long DEFAULT_MAX_RETRY_DELAY_MS = 10_000;
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    private final DefaultAzureCredential credential;

    public BlobStorageConfig() {
        // Share a single credential instance across all clients
        this.credential = new DefaultAzureCredentialBuilder().build();
    }

    public BlobServiceAsyncClient createServiceClient() {
        String endpoint = getRequiredEnv(ENDPOINT_ENV_VAR);

        int maxRetries = getEnvAsInt("AZURE_STORAGE_MAX_RETRIES", DEFAULT_MAX_RETRIES);
        long retryDelayMs = getEnvAsLong("AZURE_STORAGE_RETRY_DELAY_MS", DEFAULT_RETRY_DELAY_MS);
        long maxRetryDelayMs = getEnvAsLong("AZURE_STORAGE_MAX_RETRY_DELAY_MS", DEFAULT_MAX_RETRY_DELAY_MS);
        int timeoutSeconds = getEnvAsInt("AZURE_STORAGE_TIMEOUT_SECONDS", DEFAULT_TIMEOUT_SECONDS);
        String logLevel = System.getenv("AZURE_HTTP_LOG_LEVEL");

        RequestRetryOptions retryOptions = new RequestRetryOptions(
            RetryPolicyType.EXPONENTIAL,
            maxRetries,
            timeoutSeconds,
            retryDelayMs,
            maxRetryDelayMs,
            null
        );

        return new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .retryOptions(retryOptions)
            .httpLogOptions(new HttpLogOptions().setLogLevel(parseLogLevel(logLevel)))
            .buildAsyncClient();
    }

    DefaultAzureCredential getCredential() {
        return credential;
    }

    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required environment variable '" + name + "' is not set. "
                    + "Set it to your storage account blob endpoint, e.g. https://<account>.blob.core.windows.net");
        }
        return value;
    }

    private static int getEnvAsInt(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    private static long getEnvAsLong(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    private static HttpLogDetailLevel parseLogLevel(String level) {
        if (level == null || level.isBlank()) {
            return HttpLogDetailLevel.NONE;
        }
        return switch (level.toUpperCase()) {
            case "BASIC" -> HttpLogDetailLevel.BASIC;
            case "HEADERS" -> HttpLogDetailLevel.HEADERS;
            case "BODY" -> HttpLogDetailLevel.BODY;
            case "BODY_AND_HEADERS" -> HttpLogDetailLevel.BODY_AND_HEADERS;
            default -> HttpLogDetailLevel.NONE;
        };
    }
}
