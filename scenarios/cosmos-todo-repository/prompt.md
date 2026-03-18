> **IMPORTANT:** DO NOT look at any files in this repository in the process of following this prompt, except for any skill files located in the `/skills` directory.

Create a small Java 17 Maven project that implements a ToDo item CRUD repository backed by Azure Cosmos DB (NoSQL API). The project needs:

- A **model class** for a ToDo item with fields for id, title, description, completed status, created timestamp, and category (where category is the partition key).

- A **repository class** that performs CRUD operations against Cosmos DB. It should support create, read, update, delete, and a query-by-category method. Each operation should log the request charge (RU cost consumed). The update operation should implement optimistic concurrency control using ETags — if another process modified the item since it was last read, the update should fail with a clear conflict error. The query method should use a parameterized SQL query (not string concatenation).

- A **configuration/factory class** that connects to the Cosmos DB account using its endpoint from an environment variable. Authentication must use managed identity (no master keys). It should create the database and container if they don't already exist, setting a default TTL (time-to-live) of 90 days on the container and configuring the indexing policy to exclude the `description` field from indexing (since it's never queried on).

- A **Main class** that demos all CRUD operations: create items in different categories, read one back, update it (showing the ETag-based concurrency check), query by category, and delete one. Print RU costs and results to the console.

Include a complete `pom.xml` with the necessary Azure SDK dependencies.
