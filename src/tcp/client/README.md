# pongccheatstcp-client
A client for pongccheatstcp-server.

# Requirements
* Linux or macOS
* Rust toolchain supporting Rust 2021 (see [native](#native))
* pongccheatstcp-auth-server(-lite)
* pongccheatstcp-server

# Starting
Run `start.sh`.

# Native
Native Rust code is used to perform cryptographic operations (via Node-API) and so a supported Rust compiler and toolchain must be installed. More information can be found [here](./client_service/native/README.md#documentation).

# Documentation
Information about the TCP protocol this program implements can be found [here](../server/README.md#documentation).
