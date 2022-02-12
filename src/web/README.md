# pongccheatsweb
Web client, including a WebSocket wrapper, for pongccheatsd.

# Requirements
* Linux
* Go 1.16
* Node.js 16 (and npm)
* Python 3 and Gtk
* pongccheatsd

# Starting
Run `start.sh`.

# Usage
After starting, visit `localhost:13948` in your web browser. Start a game of Pong C and press "Attach". Press "Yes" on the confirmation dialog that appears and enter the code in your web browser. The control panel should now appear; scores can be viewed and changed. Once you are finished, press "Detach".

# Usage from other devices
While pongccheatsweb can be used on other devices, pongccheatstcp is better for this use case as it does not require on-device security confirmation (needed to avoid unauthorized usage of pongccheatsd over the network).

# Documentation
This documentation describes the WebSocket wrapper over pongccheatsd that pongccheatsdweb provides.

## Design
Unlike pongccheatstcp, pongccheatsweb does not entirely wrap pongccheatsd. Rather, it acts as a transparent proxy / bridge between a client and pongccheatsd, allowing the client to connect to the Unix domain socket over the internet while still requiring it to implement the raw pongccheatsd protocol (documentation for which can be found [here](../daemon/README.md#documentation)).

## Format
The WebSocket is located at the address `localhost:13948/ws`. All packets are sent in JSON.

Serverbound Packets (client to pongccheatsweb, **not** client to pongccheatsd):
| field name | type | comment |
| --- | --- | ---
| id | number | The packet id. Values are listened lower down |
| token | string or null | The authentication token. Proves identity to server |
| forward | string or null | The raw data to forward to pongccheatsd

Clientbound Packets (pongccheatsweb to client, although it might contain raw data from pongccheatsd)
| packet id | name | description |
| --- | --- | --- |
| 0 | ok | Operation completed successfully |
| 1 | never said hello | You must said hello to the server before proceeding |
| 2 | already said hello | You already say hello to the server |
| 3 | busy | The server already has an active client |
| 4 | not authenticated | You must authenticate to perform this action |
| 5 | too many attempts | Too many incorrect token attempts. Server had now terminated, must manually restat |
| 6 | incorrect token | Token is incorrect. Try again |
| 7 | already authenticated | No need to authenticate, already done |
| 8 | forward | Raw forwarded data from pongccheatsd |
