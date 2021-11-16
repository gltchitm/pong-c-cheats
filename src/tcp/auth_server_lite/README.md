# pongccheatstcp-auth-server-lite
Small auth server implementing the same server- and client-facing API as pongccheatstcp-auth-server (designed to be a drop-in replacement). Doesn't support nearly as many features (i.e. no web portal). Mainly useful for development and reference (<50 LoC server source allows for easy understanding).

# Requirements
* Linux
* Node.js 16 (and npm)

# Starting
Run `start.sh`.

# Usage
The server will print out a username and password upon starting. You can use these to authenticate with this server just like you would pongccheatstcp-auth-server. Runs on the same port as pongccheatstcp-auth-server meaning you can only run one or the other, not both.

# Documentation
This implements the same server- and client-facing API as pongccheatstcp-auth-server, reference that for documentation.
