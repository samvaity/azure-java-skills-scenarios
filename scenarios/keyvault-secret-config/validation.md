# Validation: keyvault-secret-config

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-security-keyvault-secrets`
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure:azure-keyvault` (old SDK)
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.security.keyvault.secrets` (not `com.microsoft.azure.keyvault`)
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential
- [ ] No hardcoded client secrets, certificates, or tenant IDs in source code
- [ ] Reads Key Vault URL from environment variable

### Anti-Pattern Checks
- [ ] No use of `KeyVaultClient` (old v7 API class)
- [ ] No use of `ServiceClientCredentials` 
- [ ] No use of `AuthenticationCallback`

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `SecretClientBuilder` (sync) and/or `SecretAsyncClient` builder (async)
- [ ] Builder chain includes `.vaultUrl(...)` and `.credential(...)`

### Secret Versioning (scenario-specific)
- [ ] Retrieves a specific version of a secret (not just latest)
- [ ] Uses `getSecret(name, version)` or equivalent API — not constructing a URL manually

### Secret Expiry Inspection (scenario-specific)
- [ ] Accesses the secret's `properties()` to get expiry date
- [ ] Uses `getExpiresOn()` or equivalent on `SecretProperties`
- [ ] Implements a configurable warning window for near-expiry detection

### Caching Layer (scenario-specific)
- [ ] Implements in-memory caching (e.g., `ConcurrentHashMap` or similar)
- [ ] Supports bulk-loading keys at startup
- [ ] Supports single-key refresh
- [ ] Integrates expiry checking with cache refresh

### Secret Rotation / LRO (scenario-specific)
- [ ] Implements secret delete as a long-running operation
- [ ] Uses `beginDeleteSecret()` and polls/waits for completion (not `deleteSecret()` fire-and-forget)
- [ ] Uses `SyncPoller` (sync) or `PollerFlux` (async) to wait for the delete to complete
- [ ] Creates new secret after delete completes (not before or concurrently)

### Error Handling
- [ ] Catches `ResourceNotFoundException` (or `HttpResponseException` with 404) for missing secrets
- [ ] Does NOT let a missing secret crash the application
- [ ] Returns a default value when secret is not found

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `SecretAsyncClient` (not sync client on a background thread)
- [ ] Uses Project Reactor types (`Mono`, `Flux`)
- [ ] Does not call `.block()` inside the async implementation
- [ ] LRO uses `PollerFlux` (async) — not `SyncPoller`

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (`com.azure.security.keyvault.secrets`) | | | Old `com.microsoft.azure:azure-keyvault` = major failure |
| `DefaultAzureCredential` used | | | |
| `SecretClientBuilder` pattern | | | |
| Secret version retrieval | | | Missing = did not address requirement |
| Secret expiry inspection | | | Missing = did not address requirement |
| Cache with bulk-load + refresh | | | |
| LRO: `beginDeleteSecret` + poller | | | Instant `deleteSecret()` = wrong |
| `ResourceNotFoundException` handling | | | Generic `Exception` = weaker |
| Async uses Reactor | | | |
| Correct Maven coordinates | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
