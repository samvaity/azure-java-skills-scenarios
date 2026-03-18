# Evaluation Guide

Step-by-step instructions for running a skills comparison experiment.

## Prerequisites

- A coding agent (GitHub Copilot in VS Code, Claude, etc.) that can write files to this repo
- Access to this repo's scenarios
- (For with-skills run) The `microsoft/skills` repo's `azure-sdk-java` skill

## Overview

Each scenario has three phases:

1. **Baseline generation** — give the prompt to the AI agent with NO skills installed
2. **With-skills generation** — give the same prompt with the `azure-sdk-java` skill installed
3. **Validation** — run the validation prompt to have an AI evaluate and compare the outputs

```
Phase 1: Baseline          Phase 2: With Skills       Phase 3: Validation
┌───────────────────┐      ┌───────────────────┐      ┌───────────────────┐
│ New session        │      │ New session        │      │ New session        │
│ No skills          │      │ Skills installed   │      │ Read validation.md │
│ Copy prompt.md     │      │ Copy prompt.md     │      │ + both outputs     │
│ → writes baseline/ │      │ → writes           │      │ → comparison report│
│                    │      │   with-skills/     │      │                    │
└───────────────────┘      └───────────────────┘      └───────────────────┘
```

## Phase 1: Run Baseline (No Skills)

**Important:** Make sure NO skills are installed in your coding agent.

1. **Start a fresh session** in your coding agent (e.g., Copilot Chat in VS Code)
2. **Verify no skills are active:**
   - In Copilot: check that no custom instructions or skills are enabled
   - In Claude: check that no project knowledge files reference skills
3. **Open this repo** as the workspace (so the agent can write files to it)
4. **Copy the full contents** of the scenario's `prompt.md` and paste it into the agent
5. The prompt instructs the agent to write its output to `baseline/sync/` and `baseline/async/`
6. Verify the agent wrote its files to the correct location
7. **Close the session entirely** — do not reuse it for the next phase

### Example

```
# For blob-storage-manager scenario:
# 1. Open VS Code with this repo
# 2. Open Copilot Chat (no skills)
# 3. Paste the contents of scenarios/blob-storage-manager/prompt.md
# 4. Agent writes files to scenarios/blob-storage-manager/baseline/sync/ and baseline/async/
# 5. Close Copilot Chat
```

## Phase 2: Run With Skills

1. **Install the Azure SDK Java skills:**

```bash
# For GitHub Copilot (via npx)
npx skills add microsoft/skills --skill azure-sdk-java
```

2. **Start a brand new session** (critical — no context bleed from the baseline run)
3. **Verify the skill is active** by checking your agent's configuration
4. **Open this repo** as the workspace
5. **Copy the exact same `prompt.md` contents** and paste it into the agent
6. The prompt instructs the agent to write its output to `with-skills/sync/` and `with-skills/async/`
7. Verify the agent wrote its files to the correct location
8. **Close the session entirely**

## Phase 3: Validate & Compare

Once both baseline and with-skills outputs exist, run the validation step.

1. **Start a brand new session** (no context from generation phases)
2. **Paste the following validation prompt**, replacing `<scenario-name>` with the scenario directory name:

```
Review the generated code in the `scenarios/<scenario-name>/` directory.

Read the file `scenarios/<scenario-name>/validation.md` for the complete
evaluation criteria.

Then evaluate each output:
1. First, evaluate `baseline/sync/` against the validation criteria
2. Then evaluate `baseline/async/` against the validation criteria
3. Then evaluate `with-skills/sync/` against the validation criteria
4. Then evaluate `with-skills/async/` against the validation criteria

Fill in every checkbox in the validation criteria for each of the four outputs.

Finally, fill in the comparison table at the bottom of validation.md,
comparing baseline vs with-skills. Write your completed evaluation as
`scenarios/<scenario-name>/evaluation-results.md`.
```

3. Review the AI-generated evaluation for accuracy (spot-check a few items manually)

## Tips

- **Use the same model** for both generation runs (baseline and with-skills) to make the comparison fair
- **Use a fresh session** for every phase — baseline, with-skills, and validation are all separate sessions
- **Be exact** with the prompt — copy-paste, don't rephrase or summarize
- **Note the model version** — LLM behavior varies across versions; record which model you used
- **Run multiple times** if you want to check consistency (LLMs are non-deterministic)
- **Don't skip the session restart** between phases — context carryover will contaminate results

## What to Look For

### Signs the skill is helping
- Uses `DefaultAzureCredential` instead of hardcoded keys or connection strings
- Imports from correct `com.azure` packages (not old `com.microsoft.azure`)
- Uses builder pattern correctly (e.g., `new BlobServiceClientBuilder().endpoint(...).credential(...).buildClient()`)
- Handles SDK-specific exceptions (e.g., `BlobStorageException`, `CosmosException`, `ServiceBusException`)
- Uses current API versions and method names
- Correct Maven `groupId`/`artifactId` (e.g., `com.azure:azure-storage-blob`, not `com.microsoft.azure:azure-storage`)
- Async implementation uses Reactor types (`Mono`, `Flux`) — the Azure SDK async surface is Reactor-based
- Proper partition key usage (Cosmos DB scenarios)
- Correct message completion/abandonment (Service Bus scenarios)
- LRO handling with `SyncPoller`/`PollerFlux` (Key Vault scenarios)

### Common LLM failure modes (without skills)
- Mixes old Azure SDK (v8) with new (v12+) patterns
- Uses deprecated classes like `CloudStorageAccount`
- Doesn't use `DefaultAzureCredential` when appropriate
- Generic exception handling instead of SDK-specific
- Missing or incorrect Maven dependency declarations
- Incorrect import paths
- Uses connection strings instead of `DefaultAzureCredential` with endpoint URLs
- Async implementation uses `CompletableFuture` or thread pools instead of Reactor
- Invents/hallucinates SDK classes or methods that don't exist
