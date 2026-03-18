> **IMPORTANT:** DO NOT look at any files in this repository in the process of following this prompt, except for any skill files located in the `/skills` directory.

Create a small Java 17 Maven project that uploads files to Azure Blob Storage with client-side encryption, where the encryption key material is managed in Azure Key Vault. The project needs:

- A **key management class** that interacts with Azure Key Vault's Keys service (not Secrets) to perform cryptographic operations. It should use Key Vault's server-side key wrap/unwrap capability to protect a locally-generated data encryption key (DEK). The workflow: generate a random AES-256 DEK locally, then call Key Vault to wrap (encrypt) that DEK with a vault-managed RSA key. Store the wrapped DEK alongside the blob. For decryption, send the wrapped DEK to Key Vault to unwrap it, then use the recovered DEK to decrypt locally. This way the raw DEK never has to be stored anywhere, and the RSA key material never leaves Key Vault.

- A **blob uploader/downloader class** that handles the actual encryption and storage. For upload: generate a DEK, encrypt the data with AES-GCM (including a random IV), wrap the DEK via Key Vault, then upload the ciphertext to Blob Storage with the wrapped DEK, IV, and vault key identifier stored as blob metadata. For download: read the blob and its metadata, unwrap the DEK via Key Vault, and decrypt. Should handle errors from both services (e.g., the vault key may have been disabled, or the blob may not exist).

- A **configuration class** that builds the necessary Azure clients for both Blob Storage and Key Vault. It should read endpoints from environment variables and authenticate with managed identity. Both clients should share a single credential instance.

- A **Main class** that demos the full round-trip: encrypts and uploads a sample document, then downloads and decrypts it, verifying the content matches. Print the vault key ID used, the wrapped DEK (base64), and the decrypted output.

Include a complete `pom.xml` with the necessary Azure SDK dependencies.
