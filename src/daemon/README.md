# pongccheatsd
Daemon implementing cheating functionality for Pong C.

# Requirements
* Linux
* Rust toolchain supporting Rust 2021
* Must be run as root using sudo (running without sudo even if you are root will not work)
* Needs to be able to use ptrace on Pong C

# Starting
Run `start.sh`

# Documentation

## How the cheating itself occurs
pongccheatsd does not actually *modify* the score (the `left_score` and `right_score` variables in Pong C). Rather, it intercepts calls to `draw_scoreboard` in Pong C using ptrace and changes the scores about to be rendered, causing the score to appear to change.

## Unix Domain Socket IPC Protocol
The point of pongccheatsd is to allow clients to access pongccheats without needing to be run as root. Clients can communicate with pongccheatsd using the protocol outlined here.

### Format
All communication is done over a Unix domain socket placed at `/tmp/pongccheatsd.sock`. Packets are sent JSON-encoded with a newline (`\n`) appended at the end. The client sends a serverbound packet and the daemon responds with a clientbound packet. The daemon will only ever send a clientbound packet in response to a serverbound packet.

This is the format of a serverbound packet (message to daemon):
| field name | type | comment |
| --- | --- | --- |
| id | number | The packet id. Values are listed lower down |
| score | number or null | The new score (null if not applicable). In range [0, 999999] |

This is the format of a clientbound packet (response from daemon):
| field name | type | comment |
| --- | --- | --- |
| error | number | The packet id. Values are listed lower down. Contrary to the name, not always an error |
| score | number | The requested score (null if not applicable) |

### Packet IDs
Serverbound Packets
| packet id | name | description |
| --- | --- | --- |
| 0 | attach | Attaches to Pong C. Must be called before anything else |
| 1 | change left score | Changes the left score |
| 2 | change right score | Changes the right score |
| 3 | get left score | Gets the left score |
| 4 | get right score | Gets the right score |
| 5 | detach | Detach from Pong C |
| 6 | detach and exit | Detach from Pong C and terminate the daemon |

Clientbound Packets
| error id | name | description |
| --- | --- | --- |
| 0 | ok | Operation completed successfully |
| 1 | left score | The left player's score |
| 2 | right score | The right player's score |
| 3 | busy | Daemon is busy |
| 4 | already attached | Already attached to Pong C |
| 5 | not attached | Not attached to Pong C |
| 6 | not found | Cannot find Pong C to attach to |
| 7 | malformed packet | Malformed serverbound packet |

### Example Communication
This is a example communication between pongccheatsd (D) and a client (C):
```
C->D: ATTACH
D->C: OK
C->D: GET LEFT SCORE
D->C: LEFT SCORE 10
C->D: GET RIGHT SCORE
D->C: RIGHT SCORE 20
C->D: SET LEFT SCORE 500
D->C: OK
C->D: GET LEFT SCORE
D->C: LEFT SCORE 500
C->D: GET LEFT SCORE
D->C: LEFT SCORE 501
C->D: DETACH
D->C: OK
C->D: GET RIGHT SCORE
D->C: NOT ATTACHED
```
