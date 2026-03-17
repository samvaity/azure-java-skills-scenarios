# Scenarios

This directory contains test scenarios organized by Azure service. Each scenario is a markdown file describing a coding task to give an LLM, along with criteria to evaluate the response.

## Directory Layout

```
scenarios/
├── _template.md      # Copy this to create a new scenario
├── storage/           # Azure Blob Storage scenarios
├── identity/          # Azure Identity scenarios
└── keyvault/          # Azure Key Vault scenarios
```

## How to Use

1. **Copy the template:** `cp _template.md storage/my-scenario.md`
2. **Fill in all sections** — especially the Prompt, Expected Behavior, and Red Flags
3. **Test it yourself** — run the prompt against an LLM to make sure it's a good test

## Writing Good Scenarios

### Prompts Should Be

- **Self-contained** — no external context needed
- **Specific** — "Create a BlobServiceClient using DefaultAzureCredential" not "do some blob stuff"
- **Realistic** — tasks a real developer would encounter

### Expected Behavior Should Be

- **Verifiable** — can you check yes/no by reading the code?
- **Important** — not style preferences, but correctness/best-practice items

### Good Scenario Ideas

Cross-cutting scenarios are especially valuable:

| Scenario Type | Example |
|---------------|---------|
| Auth + Storage | Authenticate with DefaultAzureCredential, upload a blob |
| KeyVault + Identity | Store a secret, retrieve it using managed identity |
| Storage + KeyVault | Use a Key Vault key to encrypt blob storage data |
| All three | App that reads config from Key Vault, uses identity for auth, stores data in blob |

### Scenario Complexity Levels

- **Easy:** Single SDK operation (e.g., "create a BlobServiceClient")
- **Medium:** Multiple operations combined (e.g., "upload a blob with metadata and SAS token")
- **Hard:** Cross-service, nuanced patterns (e.g., "set up managed identity auth chain for storage with Key Vault-backed secrets")
