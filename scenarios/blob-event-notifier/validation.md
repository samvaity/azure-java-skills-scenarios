# Validation: blob-event-notifier

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-messaging-eventgrid`
- [ ] Contains `com.azure:azure-storage-blob`
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.messaging.eventgrid` (not fabricated package names)
- [ ] Imports from `com.azure.storage.blob`
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential
- [ ] No hardcoded access keys, connection strings, or SAS tokens
- [ ] Reads endpoints from environment variables

### Anti-Pattern Checks
- [ ] No use of `CloudStorageAccount` or `CloudBlobClient`
- [ ] No fabricated Event Grid classes that don't exist in the SDK

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `BlobServiceClientBuilder` for Blob Storage
- [ ] Uses `EventGridPublisherClientBuilder` for Event Grid publishing
- [ ] Both builders use `.endpoint(...)` and `.credential(...)`

### Event Grid Schema Support (scenario-specific — critical)
- [ ] Handles Event Grid native schema (`EventGridEvent`)
- [ ] Handles CloudEvents 1.0 schema (`CloudEvent`)
- [ ] Uses `EventGridEvent.fromString()` or equivalent for deserialization
- [ ] Uses `CloudEvent.fromString()` or equivalent for CloudEvents deserialization
- [ ] Does NOT manually parse JSON without the SDK's deserialization helpers

### Event Routing (scenario-specific)
- [ ] Routes events based on event type string
- [ ] Handles `Microsoft.Storage.BlobCreated` events
- [ ] Handles `Microsoft.Storage.BlobDeleted` events
- [ ] Logs a warning for unrecognized event types (not silently ignoring)

### Blob Subject Parsing (scenario-specific)
- [ ] Parses container name and blob name from the event subject
- [ ] Subject pattern: `/blobServices/default/containers/{container}/blobs/{blob}`
- [ ] Parsing is robust (handles nested blob paths with `/` in the name)

### Event Publishing (scenario-specific)
- [ ] Uses `EventGridPublisherClient` to publish custom events
- [ ] Sets a subject hierarchy for filtering (e.g., "/documents/invoices/processed")
- [ ] Creates properly structured custom events with all required fields

### Blob Access Tier (scenario-specific)
- [ ] Retrieves and prints the blob's access tier for created events
- [ ] Uses blob properties to get the access tier

### Race Condition Handling (scenario-specific)
- [ ] Handles the case where the blob no longer exists (already deleted)
- [ ] Catches `BlobStorageException` with 404 status code
- [ ] Does not crash on the race condition — logs a warning or handles gracefully

### Error Handling
- [ ] Catches `BlobStorageException` for blob errors
- [ ] Catches Event Grid-specific exceptions for publishing errors
- [ ] Does not use bare `Exception` catches

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `BlobAsyncClient` and `EventGridPublisherAsyncClient`
- [ ] Uses Project Reactor types (`Mono`, `Flux`)
- [ ] Does not call `.block()` inside the async implementation

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDKs (eventgrid + blob + identity) | | | |
| `DefaultAzureCredential` used | | | |
| Event Grid schema deserialization | | | Manual JSON parse = weaker |
| CloudEvents 1.0 support | | | Missing = did not address requirement |
| Both `EventGridEvent` and `CloudEvent` | | | Only one = partial |
| Event routing by type | | | |
| Subject parsing for container/blob | | | |
| Event publishing with subject hierarchy | | | Missing = did not address requirement |
| Blob access tier reported | | | Missing = did not address requirement |
| Race condition (404) handled | | | Crash on missing blob = failure |
| Async implementation present | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
