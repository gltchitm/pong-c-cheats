# pongccheatstcp
Allows for connections to pongccheatsd over TCP. Useful for controlling Pong C from other devices.

# Information
pongccheatstcp is split into multiple different parts:

* pongccheatstcp-auth-server
* pongccheatstcp-auth-server-lite
* pongccheatstcp-client
* pongccheatstcp-server

You can choose to use either pongccheatstcp-auth-server OR pongccheatstcp-auth-server-lite, but everything else is required.

pongccheatstcp-client can be run a remote device running either Linux or macOS as long as it can connect to the required ports on the device running Pong C and the other programs needed to run pongccheatstcp.

# Starting
Read the documentation for pongccheatstcp-auth-server(-lite), pongccheatstcp-client, and pongccheatstcp-server, and follow their respective starting directions.

# Documentation
Documentation for the auth server API can be found in pongccheatstcp-auth-server and documentation for the TCP protocol used for communication between the server and client can be found in pongccheatstcp-server.
