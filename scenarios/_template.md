# Scenario: [Short Descriptive Title]

## Description

<!-- What does this scenario test? What Azure SDK task is the LLM being asked to perform? -->

## Services Involved

<!-- List all Azure services this scenario touches -->
- Azure [Service Name]
- Azure [Service Name] (if cross-cutting)

## Prompt

<!-- The exact prompt to give to the LLM. Keep it self-contained. -->

```
[Paste the prompt here]
```

## Expected Behavior

<!-- What should a correct/good response include? Be specific. -->

- [ ] Uses `DefaultAzureCredential` (not hardcoded keys)
- [ ] Imports from correct packages (`com.azure.*`)
- [ ] Uses the builder pattern for client construction
- [ ] Includes proper error handling with SDK-specific exceptions
- [ ] [Add more items specific to this scenario]

## Red Flags

<!-- Known antipatterns or incorrect approaches the LLM might produce -->

- [ ] Uses deprecated APIs or removed classes
- [ ] Hardcodes connection strings or keys
- [ ] Uses wrong package imports (e.g., mixing v1/v2 APIs)
- [ ] [Add more items specific to this scenario]

## Difficulty

<!-- How hard is this scenario for an LLM? -->
- [ ] Easy — common pattern, well-documented
- [ ] Medium — requires combining multiple concepts
- [ ] Hard — nuanced, cross-cutting, easy to get wrong

## Notes

<!-- Any additional context: why this scenario matters, known LLM failure modes, etc. -->
