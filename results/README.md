# Results

This directory stores LLM outputs from running scenarios, organized into baseline (no skills) and with-skills runs.

## Directory Layout

```
results/
├── baseline/          # LLM output WITHOUT skills installed
│   ├── storage/
│   ├── identity/
│   └── keyvault/
└── with-skills/       # LLM output WITH azure-sdk-java skill installed
    ├── storage/
    ├── identity/
    └── keyvault/
```

## Recording Results

### File Naming

Mirror the scenario filename, adding the date and model used:

```
results/baseline/storage/upload-blob-with-metadata-2026-03-17-gpt4o.md
results/with-skills/storage/upload-blob-with-metadata-2026-03-17-gpt4o.md
```

### What to Include

Each result file should contain:

```markdown
# Result: [Scenario Name]

**Date:** 2026-03-17
**Model:** GPT-4o / Claude Sonnet / etc.
**Skills installed:** None (baseline) / azure-sdk-java
**Scenario:** scenarios/storage/upload-blob-with-metadata.md

## LLM Output

[Paste the raw LLM output here]

## Evaluation

### Expected Behavior Checklist
- [x] Uses DefaultAzureCredential ✅
- [ ] Proper error handling ❌ — used generic Exception catch
- [x] Correct imports ✅

### Red Flags Found
- [ ] None found ✅
  OR
- [x] Used deprecated API ❌ — used CloudStorageAccount (v8 API)

### Overall Assessment
[Brief qualitative assessment: Did the skill help? What was better/worse?]
```

## Comparing Results

When both baseline and with-skills results exist for a scenario, compare:

1. **Correctness** — fewer red flags?
2. **Best practices** — more expected behaviors met?
3. **Code quality** — more idiomatic, modern SDK usage?
4. **Completeness** — handles edge cases, error handling?
