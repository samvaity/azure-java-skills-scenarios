> **IMPORTANT:** DO NOT look at any files in this repository in the process of following this prompt, except for any skill files located in the `/skills` directory.

Create a small Java 17 Maven project that provides a reusable Azure Blob Storage management utility. The project needs:

- A **service class** that wraps blob operations: upload (with optional metadata and blob index tags for later querying), download, list blobs in a container, and delete. The upload method should handle large files efficiently — if the file is over a configurable size threshold, it should use parallel block uploads with a configurable block size rather than a single-shot upload. The service should also support acquiring a lease on a blob before overwriting it, so that concurrent writers don't clobber each other.

- A **configuration class** that connects to Azure securely using the storage account endpoint (from an environment variable). No connection strings or account keys should be used — the app will run in Azure with managed identity.

- A **Main class** that wires everything together and demos each operation: uploads a sample file with some index tags, lists blobs, downloads the file back, acquires a lease and overwrites it, and finally deletes it. Print status at each step.

Include a complete `pom.xml` with the necessary Azure SDK dependencies.
