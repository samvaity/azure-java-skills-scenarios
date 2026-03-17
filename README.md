# azure-java-skills-scenarios

Private repo for measuring whether [microsoft/skills](https://github.com/microsoft/skills) Java SDK skills improve LLM-generated code quality.

## What is this?

The `microsoft/skills` repo contains auto-generated "skills" — structured knowledge files that ground coding agents (Copilot, Claude, etc.) with Azure SDK best practices. This repo stores **test scenarios** that let us measure whether those skills actually move the needle.

## The Experiment

```
┌─────────────────────┐    ┌──────────────────────┐
│  1. BASELINE        │    │  2. WITH SKILLS       │
│                     │    │                       │
│  Give LLM a prompt  │    │  Install azure-sdk-   │
│  (no skills)        │    │  java skill, then     │
│  → Record output    │    │  give same prompt     │
│                     │    │  → Record output      │
└────────┬────────────┘    └────────┬──────────────┘
         │                          │
         └──────────┬───────────────┘
                    ▼
           3. COMPARE & EVALUATE
           Did the skill help?
```

## Repo Structure

```
scenarios/           # Test scenarios organized by Azure service
  ├── storage/       # Azure Blob Storage scenarios
  ├── identity/      # Azure Identity scenarios
  └── keyvault/      # Azure Key Vault scenarios

results/             # Evaluation results
  ├── baseline/      # LLM output WITHOUT skills
  └── with-skills/   # LLM output WITH skills

docs/                # Guides and documentation
  └── evaluation-guide.md
```

## Quick Start

1. **Pick a scenario** from `scenarios/` (or create one using the [template](scenarios/_template.md))
2. **Run baseline** — give the prompt to Copilot/LLM without any skills installed. Save output to `results/baseline/`
3. **Install skills** — `npx skills add microsoft/skills --skill azure-sdk-java`
4. **Run with skills** — give the same prompt. Save output to `results/with-skills/`
5. **Compare** — check the expected behavior checklist and red flags

See [docs/evaluation-guide.md](docs/evaluation-guide.md) for the full step-by-step process.

## Adding Scenarios

See [CONTRIBUTING.md](CONTRIBUTING.md) and [scenarios/README.md](scenarios/README.md).

## Team

| Name | Role |
|------|------|
| Sameeksha Vaity | Java SDK SME, Scenario author |
| Jonathan Giles | Java SDK SME, Scenario author |
| Kay Venkatrajan | Skills coordination |
| Josh Free | Executive sponsor |

## Related Repos

- [microsoft/skills](https://github.com/microsoft/skills) — The skills repo (source of truth for skill content)
- [Azure/azure-sdk-for-java](https://github.com/Azure/azure-sdk-for-java) — Azure SDK for Java
