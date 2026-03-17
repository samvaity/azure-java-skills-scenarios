# Evaluation Guide

Step-by-step instructions for running a skills comparison experiment.

## Prerequisites

- A coding agent / LLM (GitHub Copilot, Copilot Chat, Claude, etc.)
- Access to this repo's scenarios
- (For with-skills run) The `microsoft/skills` repo's `azure-sdk-java` skill

## Step 1: Choose a Scenario

Pick a scenario from `scenarios/` or create one using `scenarios/_template.md`.

Example: `scenarios/storage/upload-blob-with-metadata.md`

## Step 2: Run Baseline (No Skills)

**Important:** Make sure NO skills are installed in your coding agent.

1. Open your coding agent (e.g., Copilot Chat in VS Code, or a fresh session)
2. Verify no skills are active:
   - In Copilot: check that no custom instructions or plugins are enabled
   - In Claude: check that no project knowledge files reference skills
3. Copy the **Prompt** from the scenario file
4. Paste it into the LLM and get the response
5. Save the response to `results/baseline/<service>/<scenario-name>-<date>-<model>.md`
6. Evaluate against the Expected Behavior checklist and Red Flags

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
3. Copy the **same Prompt** from the scenario file
4. Paste it into the LLM and get the response
5. Save the response to `results/with-skills/<service>/<scenario-name>-<date>-<model>.md`
6. Evaluate against the Expected Behavior checklist and Red Flags

## Step 5: Compare

Open both result files side by side and compare:

| Criteria | Baseline | With Skills |
|----------|----------|-------------|
| Expected behaviors met | _/_ | _/_ |
| Red flags found | _/_ | _/_ |
| Uses modern APIs | ☐ | ☐ |
| Proper auth pattern | ☐ | ☐ |
| Error handling | ☐ | ☐ |
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
- Uses builder pattern correctly
- Handles SDK-specific exceptions (e.g., `BlobStorageException`, `KeyVaultErrorException`)
- Uses current API versions and method names

### Common LLM failure modes (without skills)
- Mixes old Azure SDK (v8) with new (v12+) patterns
- Uses deprecated classes like `CloudStorageAccount`
- Doesn't use `DefaultAzureCredential` when appropriate
- Generic exception handling instead of SDK-specific
- Missing Maven/Gradle dependency declarations
- Incorrect import paths
