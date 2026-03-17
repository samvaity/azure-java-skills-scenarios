# Contributing to azure-java-skills-scenarios

## How to Add a Scenario

1. Copy `scenarios/_template.md` to the appropriate service directory (e.g., `scenarios/storage/`)
2. Give it a descriptive kebab-case filename (e.g., `upload-blob-with-metadata.md`)
3. Fill in all sections of the template
4. Submit a PR

## Naming Conventions

### Scenario Files

Use kebab-case names that describe the task:
```
scenarios/storage/upload-blob-with-metadata.md
scenarios/identity/default-credential-multi-tenant.md
scenarios/keyvault/store-and-retrieve-secret.md
```

### Cross-Cutting Scenarios

For scenarios that span multiple services (e.g., "store a secret in Key Vault, retrieve it, use it to auth to Storage"), place the file in the **primary** service directory and note the other services in the "Services Involved" section:
```
scenarios/keyvault/secret-to-storage-auth.md
```

### Results Files

Mirror the scenario path, adding the date and model:
```
results/baseline/storage/upload-blob-with-metadata-2026-03-17-gpt4o.md
results/with-skills/storage/upload-blob-with-metadata-2026-03-17-gpt4o.md
```

## Workflow

1. **Write the scenario** in `scenarios/`
2. **Run baseline** (no skills) — record in `results/baseline/`
3. **Run with skills** — record in `results/with-skills/`
4. **Compare** — note observations in the results files

## Scenario Quality Checklist

Before submitting a scenario, ensure:

- [ ] The prompt is clear and self-contained (someone unfamiliar can run it)
- [ ] Expected behavior items are specific and verifiable
- [ ] Red flags list real antipatterns (not just style preferences)
- [ ] The scenario tests something meaningful (not trivially simple)
- [ ] Cross-cutting services are noted if applicable

## Questions?

Reach out to Sameeksha or Jonathan in the team channel.
