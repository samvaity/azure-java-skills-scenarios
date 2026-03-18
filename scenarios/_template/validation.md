# Validation: [Scenario Name]

<!-- Replace everything in [brackets] with scenario-specific content. -->

## 1. Automated Checks

Run these against each generated project (baseline and with-skills):

| # | Check | Command / What to Look For |
|---|-------|---------------------------|
| 1 | **Correct dependencies** | `pom.xml` includes [list expected azure-sdk BOM and service artifacts] |
| 2 | **Correct imports** | All Azure imports start with `com.azure.` (not deprecated or third-party paths) |
| 3 | **Authentication** | Uses `DefaultAzureCredential` or appropriate credential from `com.azure.identity` |
| 4 | **No banned patterns** | No connection-string auth, no `new DefaultAzureCredential()` constructor, no raw HTTP calls |
| 5 | **Compiles** | `mvn compile` succeeds (or at minimum: no obvious syntax errors) |

## 2. SDK Usage Quality

Check each item; mark Y (present), N (missing), or P (partial):

| # | Criterion | What to Look For |
|---|-----------|-----------------|
| 1 | [Criterion name] | [What it means for this scenario] |
| 2 | [Criterion name] | [What it means for this scenario] |
| 3 | [Criterion name] | [What it means for this scenario] |
| 4 | [Criterion name] | [What it means for this scenario] |

## 3. Async Implementation Quality

| # | Check | What to Look For |
|---|-------|-----------------|
| 1 | **Separate subdirectory** | `sync/` and `async/` exist as separate directories |
| 2 | **Async client used** | Async variant uses the `Async` version of the service client |
| 3 | **Reactive chain** | Async code returns `Mono`/`Flux` and composes operators (no `.block()` in library code) |
| 4 | **Error handling** | Async code uses `.onErrorResume()` / `.doOnError()` (not try-catch around `.block()`) |

## 4. Comparison Table

Fill in after evaluating both outputs:

| Criterion | Baseline | With Skills | Notes |
|-----------|----------|-------------|-------|
| Automated checks pass | | | |
| [Criterion 1] | | | |
| [Criterion 2] | | | |
| [Criterion 3] | | | |
| Async quality | | | |
| **Overall** | | | |
