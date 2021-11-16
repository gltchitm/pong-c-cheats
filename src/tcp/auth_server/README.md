# pongccheatstcp-auth-server
This program allows clients to authenticate with servers.

# Requirements
* Linux
* Supported JVM
* Maven

# Why accounts instead of one-time tokens like pongccheatsweb?
Accounts allow for connections from remote devices without access to the host device while still maintaining security.

# Starting
Run `start.sh`.

This program supports passing flags to modify functionality. You can use these flags by passing them to the `start.sh` script. The flags are documented below.

| name | description |
| --- | --- |
| `--disable-signups` | Prevents signing up, still allows logging in, changing passwords, etc |
| `--disable-web-portal` | Disables the web portal entirely, still allows clients to authenticate and servers to redeem tokens |

# Usage
This program provides a web portal to create and control accounts. Once started, visit it at `localhost:13949`. You can create an account (choose a secure password) and log in to the account. The portal shown once you have logged in to your account allows you to change the password, delete the account, revoke all sessions, and log out. Note that revoking all sessions does not revoke the sesison of clients already authenticated with pongccheatstcp-server.

# Documentation
This documentation documents the web API used by clients and servers to communicate with pongccheatstcp-auth-server. The API documented here is also implemented by pongccheatstcp-auth-server-lite. It intentionally does not document features of pongccheatstcp-auth-server's web portal, including APIs to log in, change passwords, and delete accounts. Read the source code of `WebApiController.kt` for learn about these undocumented APIs.

## Format
All requests are sent in JSON over HTTP to the server on port `13949`

## Endpoints
**POST** `/api/client/login`

Returns a one-time token that can be used to authenticate with pongccheatstcp-server. Reponds with 200 if authentication is successful, other status codes indicate a failure.

Tokens returned by this specific auth server (pongccheatstcp-auth-server) last for a maximum of 30 seconds, although this is an implementation-specific detail.

Request Parameters
| name | type | description |
| --- | --- | --- |
| username | string | The username |
| password | string | The password |

Response Parameters
| name | type | description |
| --- | --- | --- |
| message | string or null | The error message (if there is one) |
| token | string or null | The token (if successful) |

**POST** `/api/server/redeem_token`

Used to verify that the token sent from a client to a server is legitimate. Responds with 200 if the token is legitimate, other status codes indicate a failure.

This specific auth server (pongccheatstcp-auth-server) destroys tokens after they are redeemed, although this is an implementation-specific detail.

Request Parameters
| name | type | description |
| --- | --- | --- |
| token | string | The token to verify |
| password | string | The password |

Response Parameters
| name | type | description |
| --- | --- | --- |
| message | string or null | The error message (if there is one) |
