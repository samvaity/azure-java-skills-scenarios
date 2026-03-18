# Validation: identity-credential-chain

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Does NOT contain `com.azure:azure-security-keyvault-*` or other service SDKs (this scenario is identity-only)
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.identity`
- [ ] Imports from `com.azure.core.credential` (for `TokenCredential`, `TokenRequestContext`, `AccessToken`)
- [ ] No imports from `com.microsoft.azure.*`
- [ ] No imports from `com.azure.identity.implementation` (internal packages)

### Auth Pattern
- [ ] No hardcoded client secrets, certificates, or tenant IDs in source code
- [ ] User-assigned managed identity client ID comes from environment variable (not hardcoded)

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Credential Chain Construction
- [ ] Uses `ChainedTokenCredentialBuilder` to compose multiple credentials
- [ ] Credentials are added via `.addLast()` or equivalent — order matters

### Local Development Chain (scenario-specific)
- [ ] Chains credentials suitable for developer machines
- [ ] Includes `AzureCliCredential` (most common dev credential)
- [ ] May include `IntelliJCredential`, `VisualStudioCodeCredential`, or `AzurePowerShellCredential`
- [ ] Order makes sense (most-likely-available first)

### CI Pipeline Chain (scenario-specific)
- [ ] Uses `EnvironmentCredential` or `AzurePipelinesCredential` for CI
- [ ] `AzurePipelinesCredential` is the correct choice for Azure Pipelines service connections
- [ ] Does NOT use `DefaultAzureCredential` as the CI credential (too broad, defeats the purpose of a targeted chain)

### Production Chain (scenario-specific)
- [ ] Uses `ManagedIdentityCredential` as the primary production credential
- [ ] Supports user-assigned managed identity (passes client ID from `AZURE_CLIENT_ID` env var)
- [ ] Uses `ManagedIdentityCredentialBuilder().clientId(...)` for user-assigned identity
- [ ] Falls back to `WorkloadIdentityCredential` for Kubernetes scenarios
- [ ] Credential ordering: managed identity first, workload identity second

### Continuous Access Evaluation / CAE (scenario-specific)
- [ ] Enables CAE on credentials or token request
- [ ] Uses `TokenRequestContext.setCaeEnabled(true)` or `enableCae()` on credential builders
- [ ] This is a recent/advanced feature — baseline may miss it entirely

### Environment Detection (scenario-specific)
- [ ] Detects CI environment (checks for `CI`, `AZURE_PIPELINE_WORKSPACE`, `TF_BUILD`, or similar)
- [ ] Detects production/managed identity (checks for `IDENTITY_ENDPOINT`, `MSI_ENDPOINT`, or similar)
- [ ] Falls back to dev if neither is detected
- [ ] Logic is reasonable and documented

### Token Request & Connectivity Testing (scenario-specific)
- [ ] Creates a `TokenRequestContext` with the correct scope (e.g., `https://management.azure.com/.default`)
- [ ] Calls `getToken()` (sync) or `getToken()` returning a reactive type (async)
- [ ] Prints token expiry time from `AccessToken.getExpiresAt()`
- [ ] Handles authentication failure with specific exception info (not generic catch)
- [ ] Reports the credential that failed and why

### Error Handling
- [ ] Catches `CredentialUnavailableException` for missing credentials
- [ ] Catches `AuthenticationRequiredException` where appropriate
- [ ] Provides actionable error messages (e.g., "AzureCli not logged in" rather than "auth failed")

## Async Implementation Quality
- [ ] Async connectivity tester exists in a separate subdirectory
- [ ] Token request uses the reactive `getToken()` method (returns `Mono<AccessToken>`)
- [ ] Does not call `.block()` inside the async implementation
- [ ] Properly handles async errors

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (`com.azure:azure-identity`) | | | |
| `ChainedTokenCredentialBuilder` used | | | Just `DefaultAzureCredential` everywhere = lazy |
| Dev chain has `AzureCliCredential` | | | |
| CI chain has `AzurePipelinesCredential` | | | Just `EnvironmentCredential` = partial |
| Prod chain: managed identity + workload identity | | | |
| User-assigned identity with client ID | | | System-only = partial |
| CAE enabled | | | Missing = likely without skills |
| Environment detection logic | | | |
| `CredentialUnavailableException` handling | | | Generic `Exception` = weaker |
| Token expiry printed | | | |
| Async token request (reactive) | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
