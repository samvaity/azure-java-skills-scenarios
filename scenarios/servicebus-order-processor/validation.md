# Validation: servicebus-order-processor

Use this file to evaluate the generated code **after** the code generation step is complete.

## Automated Checks

### Dependency Checks (pom.xml)
- [ ] Contains `com.azure:azure-messaging-servicebus`
- [ ] Contains `com.azure:azure-identity`
- [ ] Does NOT contain `com.microsoft.azure:azure-servicebus` (old SDK)
- [ ] Does NOT contain `com.microsoft.azure` groupId anywhere
- [ ] Specifies Java 17

### Import Checks
- [ ] Imports from `com.azure.messaging.servicebus` (not `com.microsoft.azure.servicebus`)
- [ ] Imports from `com.azure.identity`
- [ ] No imports from `com.microsoft.azure.*`

### Auth Pattern
- [ ] Uses `DefaultAzureCredential` or another `com.azure.identity` credential
- [ ] Uses fully-qualified namespace (not connection string) with the credential
- [ ] No hardcoded connection strings or SAS tokens
- [ ] Reads namespace from environment variable

### Anti-Pattern Checks
- [ ] No use of `QueueClient` (old SDK class)
- [ ] No use of `IMessage` or `IMessageHandler`
- [ ] No use of `ConnectionStringBuilder`

### Compilation
- [ ] `mvn compile` succeeds for the sync implementation
- [ ] `mvn compile` succeeds for the async implementation

## SDK Usage Quality

### Client Construction
- [ ] Uses `ServiceBusClientBuilder` as the entry point
- [ ] Sender: uses `.sender().queueName(...).buildClient()` (sync) chain or async equivalent
- [ ] Processor: uses `.processor().queueName(...).processMessage(...).processError(...)` chain
- [ ] Builder includes `.fullyQualifiedNamespace(...)` and `.credential(...)`

### Batch Sending (scenario-specific)
- [ ] Creates a `ServiceBusMessageBatch` via sender
- [ ] Checks `tryAddMessage()` return value before adding each message
- [ ] Handles the case where a message doesn't fit in the batch

### Scheduled Delivery (scenario-specific)
- [ ] Implements scheduled delivery for high-priority orders
- [ ] Uses `scheduleMessage()` or `setScheduledEnqueueTime()` on the message
- [ ] Delay is approximately 30 seconds as specified

### Correlation Properties (scenario-specific)
- [ ] Sets order ID as a correlation property on the message
- [ ] Uses `setCorrelationId()` or application properties

### Dead-Letter Queue (scenario-specific)
- [ ] Explicitly dead-letters failed messages (not just abandoning)
- [ ] Uses `deadLetter()` with a reason string on the message context
- [ ] Implements reading from the dead-letter sub-queue
- [ ] Uses the dead-letter queue path (`$deadletterqueue` suffix or `SubQueue.DEAD_LETTER_QUEUE`)

### Session-Aware Receiving (scenario-specific)
- [ ] Implements session-aware message processing
- [ ] Uses `.sessionProcessor()` or session-enabled receiver configuration
- [ ] Session ID is keyed by customer name as specified

### Error Handling
- [ ] Catches `ServiceBusException` (not just `Exception`)
- [ ] Error handler in processor logs entity path and error source
- [ ] Distinguishes transient vs non-transient errors (via `isTransient()` or `getReason()`)

## Async Implementation Quality
- [ ] Async implementation exists in a separate subdirectory
- [ ] Uses `ServiceBusSenderAsyncClient` / `ServiceBusProcessorClient` (processor is inherently async)
- [ ] Uses Project Reactor types (`Mono`, `Flux`) where applicable
- [ ] Does not call `.block()` inside the async implementation

## Comparison: Baseline vs With-Skills

| Criteria | Baseline | With Skills | Notes |
|----------|----------|-------------|-------|
| Correct SDK (`com.azure:azure-messaging-servicebus`) | | | Old `com.microsoft.azure:azure-servicebus` = major failure |
| `DefaultAzureCredential` with namespace (not conn string) | | | |
| `ServiceBusClientBuilder` pattern | | | |
| Batch sending with `tryAddMessage` check | | | Missing check = messages silently dropped |
| Scheduled delivery (~30s delay) | | | Missing = did not address requirement |
| Dead-letter with reason string | | | Abandon = weaker; missing = failure |
| Dead-letter queue reading | | | Missing = did not address requirement |
| Session-aware receiving | | | Missing = did not address requirement |
| `ServiceBusException` handling | | | Generic `Exception` = weaker |
| Async implementation present | | | |
| Code compiles | | | |
| Overall quality | Low/Med/High | Low/Med/High | |
