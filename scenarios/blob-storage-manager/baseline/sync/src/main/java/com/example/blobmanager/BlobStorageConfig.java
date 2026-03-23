package com.example.blobmanager;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;

/**
 * Configuration for Azure Blob Storage using managed identity (DefaultAzureCredential).
 * Supports configurable retry policy, per-request timeout, and HTTP logging level.
 */
public class BlobStorageConfig {

    private static final String DEFAULT_ENDPOINT_ENV = "AZURE_STORAGE_ENDPOINT";
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 800;
    private static final long DEFAULT_MAX_RETRY_DELAY_MS = 60_000;
    private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 30;
    private static final HttpLogDetailLevel DEFAULT_LOG_LEVEL = HttpLogDetailLevel.HEADERS;

    private final String endpoint;
    private final int maxRetries;
    private final long retryDelayMs;
    private final long maxRetryDelayMs;
    private final int requestTimeoutSeconds;
    private final HttpLogDetailLevel logLevel;

    /** Creates a config using defaults and the AZURE_STORAGE_ENDPOINT env var. */
    public BlobStorageConfig() {
        this(
            System.getenv(DEFAULT_ENDPOINT_ENV),
            DEFAULT_MAX_RETRIES,
            DEFAULT_RETRY_DELAY_MS,
            DEFAULT_MAX_RETRY_DELAY_MS,
            DEFAULT_REQUEST_TIMEOUT_SECONDS,
            DEFAULT_LOG_LEVEL
        );
    }

    public BlobStorageConfig(String endpoint, int maxRetries, long retryDelayMs,
                             long maxRetryDelayMs, int requestTimeoutSeconds,
                             HttpLogDetailLevel logLevel) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException(
                "Storage endpoint is required. Set the " + DEFAULT_ENDPOINT_ENV + " environment variable.");
        }
        this.endpoint = endpoint;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.maxRetryDelayMs = maxRetryDelayMs;
        this.requestTimeoutSeconds = requestTimeoutSeconds;
        this.logLevel = logLevel;
    }

    /** Builds a synchronous BlobServiceClient with retry, timeout, and logging configured. */
    public BlobServiceClient createServiceClient() {
        RequestRetryOptions retryOptions = new RequestRetryOptions(
            RetryPolicyType.EXPONENTIAL,
            maxRetries,
            requestTimeoutSeconds,
            retryDelayMs,
            maxRetryDelayMs,
            null
        );

        HttpLogOptions httpLogOptions = new HttpLogOptions()
            .setLogLevel(logLevel);

        return new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .retryOptions(retryOptions)
            .httpLogOptions(httpLogOptions)
            .buildClient();
    }

    public String getEndpoint() { return endpoint; }
    public int getMaxRetries() { return maxRetries; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public long getMaxRetryDelayMs() { return maxRetryDelayMs; }
    public int getRequestTimeoutSeconds() { return requestTimeoutSeconds; }
    public HttpLogDetailLevel getLogLevel() { return logLevel; }
}
