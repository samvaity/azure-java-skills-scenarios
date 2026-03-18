# Validation: storage-keyvault-encrypted-uploader

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-storage-blob`
- [ ] Contains `com.azure:azure-security-keyvault-keys` (Keys, not Secrets — the prompt asks for key wrap/unwrap)
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.storage.blob`
- [ ] Imports from `com.azure.security.keyvault.keys` or `com.azure.security.keyvault.keys.cryptography`
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`
- [ ] Uses `javax.crypto` or `java.security` for local AES-GCM encryption

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential
- [ ] Shares a single credential instance between Blob Storage and Key Vault clients
- [ ] No hardcoded keys, connection strings, or SAS tokens
- [ ] Reads endpoints from environment variables

### Anti-Pattern Checks
- [ ] No use of `CloudStorageAccount` or `CloudBlobClient`
- [ ] No use of old Key Vault SDK classes
- [ ] No storing raw encryption key material in plaintext (the DEK should only exist wrapped)

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `BlobServiceClientBuilder` for Blob Storage
- [ ] Uses `KeyClient` / `CryptographyClient` builder for Key Vault Keys (NOT `SecretClient`)
- [ ] Both builders use `.endpoint(...)` / `.vaultUrl(...)` and `.credential(...)`

### Key Vault Keys vs Secrets (scenario-specific — critical)
- [ ] Uses the Key Vault **Keys** service (`azure-security-keyvault-keys`), NOT Secrets
- [ ] Uses `CryptographyClient` or `KeyClient` for wrap/unwrap operations
- [ ] Uses `wrapKey()` and `unwrapKey()` APIs
- [ ] Specifies an RSA key wrap algorithm (e.g., `KeyWrapAlgorithm.RSA_OAEP` or `RSA_OAEP_256`)
- [ ] The RSA key material never leaves Key Vault (wrap/unwrap happens server-side)

### Envelope Encryption Pattern (scenario-specific — critical)
- [ ] Generates a random AES-256 DEK locally (32 bytes)
- [ ] Encrypts data with AES-GCM locally using the DEK
- [ ] Wraps the DEK via Key Vault (`wrapKey`)
- [ ] Stores the wrapped (encrypted) DEK as blob metadata
- [ ] For decryption: retrieves wrapped DEK from blob metadata, unwraps via Key Vault, decrypts locally
- [ ] Stores the IV (initialization vector) alongside the blob (in metadata)
- [ ] Stores the vault key identifier in blob metadata (so you know which key to unwrap with)

### AES-GCM Encryption (scenario-specific)
- [ ] Uses AES-GCM (not AES-CBC, AES-ECB, or other modes)
- [ ] Generates a random IV for each encryption operation
- [ ] IV length is appropriate (typically 12 bytes for GCM)

### Error Handling
- [ ] Handles `BlobStorageException` for blob errors
- [ ] Handles Key Vault errors (e.g., key disabled, key not found)
- [ ] Catches specific exceptions rather than generic `Exception`

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `BlobAsyncClient` and `CryptographyAsyncClient` (or equivalent async builders)
- [ ] Uses Project Reactor types (`Mono`, `Flux`)
- [ ] Does not call `.block()` inside the async implementation

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDKs (blob + keyvault-keys + identity) | | | Using keyvault-secrets instead of keys = wrong |
| `DefaultAzureCredential` shared | | | Separate credentials = works but wasteful |
| Uses Key Vault Keys (not Secrets) | | | Secrets = fundamentally wrong approach |
| `CryptographyClient` for wrap/unwrap | | | Manual RSA = reinventing the wheel |
| Envelope encryption (DEK/KEK) pattern | | | Encrypting with vault key directly = wrong |
| AES-GCM with random IV | | | AES-CBC/ECB = weaker |
| Wrapped DEK stored as blob metadata | | | Stored in plaintext = security failure |
| Key identifier in blob metadata | | | Missing = can't decrypt later |
| Handles blob + vault exceptions | | | |
| Async implementation present | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
