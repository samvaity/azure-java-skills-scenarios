# Validation: blob-storage-manager

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

These can be verified by grep, compilation, or script.

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-storage-blob` (not `com.microsoft.azure:azure-storage` or `com.azure:azure-storage`)
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17 (`<maven.compiler.source>17</maven.compiler.source>` or equivalent)

### Import Checks
- [ ] Imports from `com.azure.storage.blob` (not `com.microsoft.azure.storage`)
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`
- [ ] No imports from `com.azure.storage` without the `.blob` subpackage (unless using common types)

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential — not connection strings
- [ ] No hardcoded account keys, connection strings, or SAS tokens in source code
- [ ] Reads storage endpoint from environment variable

### Anti-Pattern Checks
- [ ] No use of `CloudStorageAccount` (deprecated v8 API)
- [ ] No use of `CloudBlobClient` or `CloudBlobContainer`
- [ ] No use of `StorageCredentialsAccountAndKey`

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `BlobServiceClientBuilder` (sync) and/or `BlobServiceAsyncClient` builder (async)
- [ ] Builder chain includes `.endpoint(...)` and `.credential(...)`
- [ ] Async implementation uses the async variant of the client (not wrapping sync in a thread pool)

### Retry & HTTP Pipeline (scenario-specific)
- [ ] Configures a custom retry policy (exponential backoff, max retries, delay)
- [ ] Sets a per-request or per-operation timeout
- [ ] Enables HTTP logging (e.g., `HttpLogOptions` or similar)

### Blob Lease (scenario-specific)
- [ ] Implements blob lease acquisition before overwrite
- [ ] Uses a lease-specific API (not just uploading and hoping)

### Parallel Upload (scenario-specific)
- [ ] Implements parallel/block upload for large files
- [ ] Has a configurable size threshold and block size
- [ ] Uses `ParallelTransferOptions` or equivalent — not a manual chunking loop

### Blob Index Tags (scenario-specific)
- [ ] Sets blob index tags on upload (not just metadata)
- [ ] Tags are a `Map<String, String>` set via the upload options

### Error Handling
- [ ] Catches `BlobStorageException` (not just `Exception` or `RuntimeException`)
- [ ] Handles or logs the HTTP status code from storage errors

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `BlobServiceAsyncClient` / `BlobAsyncClient` (not sync client on a background thread)
- [ ] Uses Project Reactor types (`Mono`, `Flux`) — the Azure SDK async surface is Reactor-based
- [ ] Does not call `.block()` inside the async implementation (that defeats the purpose)
- [ ] Properly subscribes to reactive streams in the demo/main

## Comparison: Baseline vs With-Skills

When both outputs exist, compare:

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (v12, `com.azure`) | | | v8 `CloudStorageAccount` = major failure |
| `DefaultAzureCredential` used | | | Connection strings = failure |
| `BlobServiceClientBuilder` pattern | | | |
| `BlobStorageException` handling | | | Generic `Exception` = weaker |
| Retry policy configured | | | Missing = acceptable but weaker |
| HTTP logging configured | | | Missing = acceptable but weaker |
| Parallel upload implemented | | | Missing = did not address requirement |
| Blob lease implemented | | | Missing = did not address requirement |
| Blob index tags (not just metadata) | | | Metadata only = partial credit |
| Async uses Reactor (`Mono`/`Flux`) | | | `CompletableFuture` = wrong for Azure SDK |
| Correct Maven coordinates | | | Wrong groupId = compilation failure |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
