> **IMPORTANT:** DO NOT look at any files in this repository in the process of following this prompt, except for any skill files located in the `/skills` directory.

Create a small Java 17 Maven project that demonstrates how to correctly build Azure credential chains tailored to different deployment environments — local development, CI/CD pipelines, and production. The project needs:

- A **credential factory class** that builds the appropriate Azure credential for each environment. For local development, it should chain together credentials that work from developer tools (CLI, IDE plugins, etc.). For CI pipelines, it should support credentials sourced from pipeline environment variables or Azure Pipelines service connections. For production, it should prefer managed identity (supporting both system-assigned and user-assigned, where the user-assigned identity's client ID comes from an environment variable), with workload identity as a fallback for Kubernetes scenarios. The factory should also support enabling Continuous Access Evaluation (CAE) on the credentials, which lets Azure revoke tokens mid-session for security events.

- An **environment detector class** that auto-detects which environment the app is running in by probing for well-known environment variables (e.g., CI pipeline workspace variables, managed identity endpoint availability). It should classify the environment as dev, CI, or production.

- A **connectivity tester class** that verifies a credential works by requesting a token for a given Azure scope. It should print success/failure, the token's expiry time, and whether the token is CAE-enabled. It should handle and report the specific failure reason if authentication fails (expired cert, wrong tenant, no identity available, etc.) rather than just printing a generic error.

- A **Main class** that detects the current environment, builds the right credential, and runs the connectivity test against Azure Resource Manager. Print the detected environment, the selected credential strategy, and the test result.

Include a complete `pom.xml` with the necessary Azure SDK dependencies.
