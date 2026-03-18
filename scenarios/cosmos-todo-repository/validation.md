# Validation: cosmos-todo-repository

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-cosmos`
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure:azure-documentdb` (old SDK)
- [ ] Does NOT contain `com.microsoft.azure:azure-cosmosdb` 
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.cosmos` (not `com.microsoft.azure.documentdb`)
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential
- [ ] No hardcoded master keys or connection strings
- [ ] Reads Cosmos DB endpoint from environment variable

### Anti-Pattern Checks
- [ ] No use of `DocumentClient` (old v2 API)
- [ ] No use of `DocumentClientException`
- [ ] No connection string with `AccountKey=...`

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `CosmosClientBuilder` to build the client
- [ ] Builder chain includes `.endpoint(...)` and `.credential(...)`
- [ ] Uses `CosmosClient` (sync) and `CosmosAsyncClient` (async) — not just one

### Partition Key Handling
- [ ] Uses `PartitionKey` correctly with the `category` field
- [ ] All point operations (read, update, delete) include the partition key — not just the id
- [ ] Container creation specifies `/category` as the partition key path

### Optimistic Concurrency / ETags (scenario-specific)
- [ ] Read operation captures the ETag (from `CosmosItemResponse` or item properties)
- [ ] Update operation passes the ETag as an `ifMatchETag` option
- [ ] Handles 412 Precondition Failed (conflict) as a specific error case — not a generic exception

### Pagination (scenario-specific)
- [ ] Sync query uses `iterableByPage()` or `CosmosPagedIterable` page-level iteration
- [ ] Has configurable page size (via `QueryRequestOptions.setMaxItemCount` or `CosmosQueryRequestOptions`)
- [ ] Logs or prints continuation token per page
- [ ] Logs item count per page
- [ ] Does NOT just call `.stream()` or `.forEach()` flattening all results

### Async Pagination (scenario-specific)
- [ ] Async query uses `CosmosPagedFlux` or returns pages as a stream
- [ ] Caller can process pages individually as they arrive

### Parameterized Query (scenario-specific)
- [ ] Uses `SqlQuerySpec` with `SqlParameter` for the category value
- [ ] Does NOT build the query with string concatenation (e.g., `"SELECT * FROM c WHERE c.category = '" + category + "'"`)

### TTL Configuration (scenario-specific)
- [ ] Sets default TTL on the container (90 days = 7776000 seconds)
- [ ] Uses `ContainerProperties.setDefaultTimeToLiveInSeconds()` or equivalent

### Indexing Policy (scenario-specific)
- [ ] Configures a custom indexing policy
- [ ] Excludes `/description` path from indexing
- [ ] Uses `IndexingPolicy.setExcludedPaths()` or equivalent

### RU Cost Logging (scenario-specific)
- [ ] Extracts request charge from response (e.g., `getRequestCharge()`)
- [ ] Logs/prints RU cost for each operation

### Error Handling
- [ ] Catches `CosmosException` (not just `Exception`)
- [ ] Checks status code (e.g., 404, 409, 412) for specific error handling
- [ ] Handles 412 (precondition failed) separately for ETag conflicts

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `CosmosAsyncClient` / `CosmosAsyncDatabase` / `CosmosAsyncContainer`
- [ ] Uses Project Reactor types (`Mono`, `Flux`)
- [ ] Does not call `.block()` inside the async implementation

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (`com.azure:azure-cosmos`) | | | Old `documentdb` = major failure |
| `DefaultAzureCredential` (no master key) | | | |
| `CosmosClientBuilder` pattern | | | |
| Correct partition key usage | | | Missing partition key = runtime errors |
| ETag-based optimistic concurrency | | | Missing = did not address requirement |
| Parameterized SQL query | | | String concat = SQL injection risk |
| Pagination with page-size control | | | Flattened results = did not address requirement |
| TTL configured (90 days) | | | Missing = did not address requirement |
| Indexing policy excludes `description` | | | Missing = did not address requirement |
| RU cost logged per operation | | | |
| `CosmosException` with status codes | | | Generic `Exception` = weaker |
| Async uses Reactor (`Mono`/`Flux`) | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
