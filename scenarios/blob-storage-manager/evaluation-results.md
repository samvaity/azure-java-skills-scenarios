# Evaluation Results: blob-storage-manager

## With-Skills: GOOD

- Catches `BlobStorageException` with HTTP status codes, `CredentialUnavailableException`, and `ClientAuthenticationException` — baseline had zero error handling
- Sets blob index tags atomically during upload via `BlobParallelUploadOptions.setTags()` — baseline required a separate `setTags()` API call after upload (race condition)
- Retry, timeout, and HTTP log level all configurable via environment variables at runtime — baseline hardcoded these as compile-time constants
- Uses `BlobParallelUploadOptions` with streaming `InputStream`/`BinaryData.fromFile()` for memory-efficient large file uploads — baseline used file-path-based `BlobUploadFromFileOptions`
- Shares a single `DefaultAzureCredential` instance across clients — baseline created new instances per call

## With-Skills: BAD

- Did not use `azure-sdk-bom` for dependency management — baseline correctly used BOM which ensures version consistency across Azure SDK libraries
- Did not set `maxSingleUploadSizeLong()` threshold on `ParallelTransferOptions` — baseline explicitly set 256MB threshold for single-shot vs parallel upload decision

## With-Skills: NEEDS INVESTIGATION

- Is `BlobParallelUploadOptions` with `InputStream` actually preferred over `BlobUploadFromFileOptions` with file path for large file uploads? Need to confirm with SDK team which is the recommended pattern
- Both implementations used lease-based concurrency — need to confirm if this was guided by skill content or would have happened regardless (re-run to verify)
- Baseline's non-atomic tag setting via separate `setTags()` call — is this actually a problem in practice, or does the SDK handle this gracefully?
- With-skills omitted BOM despite it being a best practice — check if the skill files should recommend BOM usage

## Both Got Right

- `DefaultAzureCredential` via `DefaultAzureCredentialBuilder` (no connection strings or keys)
- `com.azure:azure-storage-blob` / `com.azure:azure-identity` (no deprecated `com.microsoft.azure` packages)
- `BlobServiceClientBuilder` with `.endpoint()` and `.credential()`
- Reactor `Mono`/`Flux` for async (no `CompletableFuture`, no `.block()` in service code)
- `BlobLeaseClient` / `BlobLeaseAsyncClient` for concurrency prevention
- `RequestRetryOptions` with exponential backoff
- `HttpLogOptions` for HTTP logging
- Correct async client variants (`BlobServiceAsyncClient`, not sync wrapped in threads)

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (v12, `com.azure`) | ✅ | ✅ | Both correct. Baseline uses BOM, with-skills pins 12.33.0 |
| `DefaultAzureCredential` used | ✅ | ✅ | Both correct — no connection strings |
| `BlobServiceClientBuilder` pattern | ✅ | ✅ | Both correct |
| `BlobStorageException` handling | ❌ | ✅ | **gap** Baseline had zero error handling |
| Retry policy configured | ✅ | ✅ | Both correct. With-skills adds env-var configurability |
| HTTP logging configured | ✅ | ✅ | Both correct. With-skills adds env-var configurability |
| Parallel upload implemented | ✅ | ✅ | Both correct. Baseline set explicit threshold (slightly better) |
| Blob lease implemented | ✅ | ✅ | Both correct |
| Blob index tags (not just metadata) | ~Partial | ✅ | Baseline set tags in separate call (non-atomic) |
| Async uses Reactor (`Mono`/`Flux`) | ✅ | ✅ | Both correct |
| Correct Maven coordinates | ✅ | ✅ | Both correct |
| Code compiles | ✅ | ✅ | Both correct |
| Overall quality | High | High | With-skills edges ahead on error handling, tags, configurability |
