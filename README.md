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
scenarios/
├── _template/                         # Copy this to create a new scenario
├── blob-storage-manager/              # Azure Blob Storage CRUD utility
│   ├── prompt.md                      # The prompt to give the LLM
│   ├── baseline/                      # LLM output WITHOUT skills
│   └── with-skills/                   # LLM output WITH skills
├── keyvault-secret-config/            # Key Vault-backed config provider
├── cosmos-todo-repository/            # Cosmos DB CRUD repository
├── servicebus-order-processor/        # Service Bus order messaging
├── appconfig-feature-flags/           # App Configuration feature flags
├── storage-keyvault-encrypted-uploader/ # Cross-service: encrypted blob upload
├── identity-credential-chain/         # Credential chain per environment
└── blob-event-notifier/               # Blob events via Event Grid

docs/
└── evaluation-guide.md
```

Each scenario is self-contained: the `prompt.md` is the input, and `baseline/` and `with-skills/` hold the LLM outputs for comparison.

## Scenarios

| Scenario | Azure Services | Description |
|----------|---------------|-------------|
| [blob-storage-manager](scenarios/blob-storage-manager/prompt.md) | Blob Storage | Upload, download, list, delete blobs via a service class |
| [keyvault-secret-config](scenarios/keyvault-secret-config/prompt.md) | Key Vault, Identity | Config provider that reads and caches Key Vault secrets |
| [cosmos-todo-repository](scenarios/cosmos-todo-repository/prompt.md) | Cosmos DB | CRUD repository for a ToDo app with partition key handling |
| [servicebus-order-processor](scenarios/servicebus-order-processor/prompt.md) | Service Bus | Send and receive order messages with batch support |
| [appconfig-feature-flags](scenarios/appconfig-feature-flags/prompt.md) | App Configuration | Feature flag evaluation with targeting rules |
| [storage-keyvault-encrypted-uploader](scenarios/storage-keyvault-encrypted-uploader/prompt.md) | Blob Storage, Key Vault | Encrypt data with a Key Vault key, upload to Blob Storage |
| [identity-credential-chain](scenarios/identity-credential-chain/prompt.md) | Identity | Build different credential chains for dev / CI / production |
| [blob-event-notifier](scenarios/blob-event-notifier/prompt.md) | Blob Storage, Event Grid | Process blob-created/deleted events from Event Grid |

## Quick Start

1. **Pick a scenario** from the table above (or create one using the [template](scenarios/_template/prompt.md))
2. **Run baseline** — give the prompt to Copilot/LLM without any skills installed. Save output into that scenario's `baseline/` directory.
3. **Install skills** — `npx skills add microsoft/skills --skill azure-sdk-java`
4. **Run with skills** — give the same prompt in a fresh session. Save output into `with-skills/`.
5. **Compare** — evaluate both outputs against the criteria in [docs/evaluation-guide.md](docs/evaluation-guide.md)

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
