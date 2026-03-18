# Contributing to azure-java-skills-scenarios

## How to Add a Scenario

1. Copy the `scenarios/_template/` directory:
   ```bash
   cp -r scenarios/_template scenarios/my-new-scenario
   ```
2. Give it a descriptive kebab-case directory name (e.g., `cosmos-todo-repository`)
3. Replace `prompt.md` with your plain-language prompt (see guidelines below)
4. Submit a PR

## Naming Conventions

### Scenario Directories

Use kebab-case names that describe the task:
```
scenarios/blob-storage-manager/
scenarios/keyvault-secret-config/
scenarios/cosmos-todo-repository/
```

Cross-service scenarios should name both services:
```
scenarios/storage-keyvault-encrypted-uploader/
```

### Result Files

Save LLM outputs inside each scenario's `baseline/` or `with-skills/` directory, including the date and model in the filename:
```
scenarios/blob-storage-manager/baseline/2026-03-17-gpt4o.md
scenarios/blob-storage-manager/with-skills/2026-03-17-gpt4o.md
```

## Prompt Guidelines

Each `prompt.md` should be a plain natural-language request — the kind of thing you'd type into Copilot Chat or Claude. It should:

- Be **self-contained** — the LLM should need no external context
- Ask for a **small Maven project** with 2–4 Java classes
- Target **1–2 Azure services**
- Specify class names, methods, and behavior explicitly
- Mention authentication approach (`DefaultAzureCredential`)
- Specify Java version and build tool
- **Not** include evaluation criteria (those live in [docs/evaluation-guide.md](docs/evaluation-guide.md))

## Workflow

1. **Write the scenario** — create the directory and `prompt.md`
2. **Run baseline** (no skills) — save output in the scenario's `baseline/` directory
3. **Run with skills** — save output in the scenario's `with-skills/` directory
4. **Compare** — evaluate per [docs/evaluation-guide.md](docs/evaluation-guide.md)

## Scenario Quality Checklist

Before submitting a scenario, ensure:

- [ ] The prompt is clear and self-contained (someone unfamiliar can run it)
- [ ] The prompt asks for something non-trivial (multiple classes, realistic task)
- [ ] The scenario tests SDK patterns where skills should make a measurable difference
- [ ] Cross-service scenarios name all services involved

## Questions?

Reach out to Sameeksha or Jonathan in the team channel.
