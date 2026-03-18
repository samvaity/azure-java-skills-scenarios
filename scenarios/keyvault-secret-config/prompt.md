> **IMPORTANT:** DO NOT look at any files in this repository in the process of following this prompt, except for any skill files located in the `/skills` directory.

Create a small Java 17 Maven project that implements an application configuration provider backed by Azure Key Vault. The project needs:

- A **secret provider class** that retrieves secrets from Key Vault by name, with graceful handling when a secret doesn't exist (return a default value instead of crashing). It should also be able to retrieve a specific version of a secret (not just the latest), and inspect a secret's expiry date so the caller can tell if a secret is about to expire.

- A **caching layer** on top of the provider that stores secret values in memory after first retrieval. It should support bulk-loading a predefined set of required config keys at startup, on-demand refresh of individual keys, and automatic re-fetch of any secret whose expiry date is within a configurable warning window (e.g., 7 days out).

- A **configuration/factory class** that connects securely to the Key Vault using the vault URL from an environment variable. The application runs in Azure and should authenticate using managed identity — no client secrets or certificates in code.

- A **Main class** that demos: loading several config keys at startup, reading them from cache, refreshing one, and printing a warning if any secret is near expiry.

Include a complete `pom.xml` with the necessary Azure SDK dependencies.
