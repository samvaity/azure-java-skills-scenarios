# Validation: appconfig-feature-flags

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-data-appconfiguration`
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.data.appconfiguration` (not fabricated package names)
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential
- [ ] No hardcoded connection strings or access keys
- [ ] Reads App Configuration endpoint from environment variable

### Anti-Pattern Checks
- [ ] No use of connection string-based authentication
- [ ] No fabricated/hallucinated class names that don't exist in the SDK

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `ConfigurationClientBuilder` (sync) and/or `ConfigurationAsyncClient` builder (async)
- [ ] Builder chain includes `.endpoint(...)` and `.credential(...)`

### Label-Based Configuration (scenario-specific)
- [ ] Retrieves settings with a specific label parameter
- [ ] Uses `SettingSelector` or `setLabelFilter()` for label-based filtering
- [ ] Demonstrates different labels for different environments (e.g., "production", "staging")

### Prefix Listing (scenario-specific)
- [ ] Lists settings filtered by key prefix
- [ ] Uses `SettingSelector` with `setKeyFilter()` for prefix matching (e.g., `"app/*"`)
- [ ] Returns results as a map

### Conditional Reads (scenario-specific)
- [ ] Implements conditional reads to avoid re-downloading unchanged settings
- [ ] Uses `matchConditions` / `setIfNoneMatch()` with the setting's ETag
- [ ] Handles 304 Not Modified response (setting unchanged since last read)
- [ ] Caches and returns the previous value when unchanged

### Feature Flag Key Prefix (scenario-specific)
- [ ] Uses `.appconfig.featureflag/` prefix for feature flag keys
- [ ] Parses the JSON payload stored in the feature flag setting value
- [ ] Checks the `enabled` field in the JSON

### Percentage Rollout (scenario-specific)
- [ ] Implements percentage-based evaluation for feature flags
- [ ] Uses a deterministic/consistent hash of the user ID (same user → same result every time)
- [ ] Does NOT use `Math.random()` or other non-deterministic approaches

### Sentinel Key Watching (scenario-specific)
- [ ] Implements a polling loop that watches sentinel keys
- [ ] Detects when a sentinel key's value changes (via ETag comparison or value comparison)
- [ ] Triggers a full config refresh when sentinel changes
- [ ] Has a configurable polling interval

### Error Handling
- [ ] Handles missing settings gracefully (returns null, Optional, or default)
- [ ] Catches `HttpResponseException` or SDK-specific exceptions

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `ConfigurationAsyncClient` (not sync on background thread)
- [ ] Uses Project Reactor types (`Mono`, `Flux`)
- [ ] Does not call `.block()` inside the async implementation

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (`com.azure:azure-data-appconfiguration`) | | | |
| `DefaultAzureCredential` (not connection string) | | | |
| `ConfigurationClientBuilder` pattern | | | |
| Label-based setting retrieval | | | Missing = did not address requirement |
| Conditional reads with ETag/304 | | | Missing = did not address requirement |
| `.appconfig.featureflag/` prefix | | | Wrong prefix = flags won't be found |
| Percentage rollout (deterministic) | | | `Math.random()` = non-deterministic = wrong |
| Sentinel key watching pattern | | | Missing = did not address requirement |
| Async uses Reactor | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
