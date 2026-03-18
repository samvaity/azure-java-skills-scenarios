# Evaluation Guide

Step-by-step instructions for running a skills comparison experiment.

## Prerequisites

- A coding agent / LLM (GitHub Copilot, Copilot Chat, Claude, etc.)
- Access to this repo's scenarios
- (For with-skills run) The `microsoft/skills` repo's `azure-sdk-java` skill

## Step 1: Choose a Scenario

Pick a scenario from `scenarios/`. Each scenario is a directory containing a `prompt.md`.

Example: `scenarios/blob-storage-manager/prompt.md`

## Step 2: Run Baseline (No Skills)

**Important:** Make sure NO skills are installed in your coding agent.

1. Open your coding agent (e.g., Copilot Chat in VS Code, or a fresh session)
2. Verify no skills are active:
   - In Copilot: check that no custom instructions or plugins are enabled
   - In Claude: check that no project knowledge files reference skills
3. Copy the full contents of `prompt.md` from the scenario
4. Paste it into the LLM and get the response
5. Save the response to the scenario's `baseline/` directory, e.g.:
   `scenarios/blob-storage-manager/baseline/2026-03-17-gpt4o.md`
6. Evaluate against the criteria below

## Step 3: Install Skills

Install the Azure SDK Java skills:

```bash
# For GitHub Copilot (via npx)
npx skills add microsoft/skills --skill azure-sdk-java

# For Claude (via plugin)
/plugin install azure-sdk-java@skills
```

Verify the skill is active by checking your agent's configuration.

## Step 4: Run With Skills

1. Open a **new session** in your coding agent (to avoid context bleed)
2. Verify the `azure-sdk-java` skill is active
3. Copy the **same** `prompt.md` content
4. Paste it into the LLM and get the response
5. Save the response to the scenario's `with-skills/` directory, e.g.:
   `scenarios/blob-storage-manager/with-skills/2026-03-17-gpt4o.md`
6. Evaluate against the criteria below

## Step 5: Compare

Open both result files side by side and compare:

| Criteria | Baseline | With Skills |
|----------|----------|-------------|
| Uses modern APIs (v12+ / latest) | ☐ | ☐ |
| Proper auth (`DefaultAzureCredential`) | ☐ | ☐ |
| Correct `com.azure.*` imports | ☐ | ☐ |
| Builder pattern for clients | ☐ | ☐ |
| SDK-specific exception handling | ☐ | ☐ |
| Correct Maven coordinates | ☐ | ☐ |
| Code compiles / is coherent | ☐ | ☐ |
| Overall quality | Low/Med/High | Low/Med/High |

## Tips

- **Use the same model** for both runs to make the comparison fair
- **Use a fresh session** for each run to avoid context carryover
- **Be exact** with the prompt — copy-paste, don't rephrase
- **Note the model version** — LLM behavior varies across versions
- **Run multiple times** if you want to check consistency (LLMs are non-deterministic)

## What to Look For

### Signs the skill is helping
- Uses `DefaultAzureCredential` instead of hardcoded keys
- Imports from correct `com.azure` packages (not old `com.microsoft.azure`)
- Uses builder pattern correctly (e.g., `new BlobServiceClientBuilder().endpoint(...).credential(...).buildClient()`)
- Handles SDK-specific exceptions (e.g., `BlobStorageException`, `CosmosException`, `ServiceBusException`)
- Uses current API versions and method names
- Correct Maven `groupId`/`artifactId` (e.g., `com.azure:azure-storage-blob`, not `com.microsoft.azure:azure-storage`)
- Proper partition key usage (Cosmos DB scenarios)
- Correct message completion/abandonment (Service Bus scenarios)

### Common LLM failure modes (without skills)
- Mixes old Azure SDK (v8) with new (v12+) patterns
- Uses deprecated classes like `CloudStorageAccount`
- Doesn't use `DefaultAzureCredential` when appropriate
- Generic exception handling instead of SDK-specific
- Missing or incorrect Maven/Gradle dependency declarations
- Incorrect import paths
- Uses connection strings instead of `DefaultAzureCredential` with endpoint URLs
