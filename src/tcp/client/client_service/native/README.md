# pongccheatstcp-client-native
Implementaiton of XChaCha20-Poly1305 for pongccheatstcp-client via Node-API.

# Requirements
* Linux or macOS
* Rust toolchain supporting Rust 2021

# Building
Automatically built when you start pongccheatstcp-client.

# Documentation
This is the documentation for the cryptographic protocol used between pongccheatstcp-client and pongccheatstcp-server.

## Algorithm
XChaCha20-Poly1305 is used for encryption and decryption. It was selected because it is fast and it is an AEAD.

## Encryption
The input is arbitrary binary data. First, a 24-byte long n<sub>once</sub> is generated at random (safe enough for this non-critical use case given XChaCha20's extended nonce size). Next, the encryption is performed. Then, the tag is appended to the encryption data. After this, the n<sub>once</sub> is appended after the tag. Once completed, the result should be a binary output in this format: `encrypted data || tag || nonce`.
