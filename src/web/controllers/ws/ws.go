package ws

import (
	"crypto/rand"
	"math/big"
	"net/http"
	"os"
	"os/exec"

	"github.com/gin-gonic/gin"
	"github.com/gltchitm/pong-c-cheats/web/communicator"
	"github.com/gorilla/websocket"
)

const (
	SERVERBOUND_HELLO = iota
	SERVERBOUND_AUTHENTICATE
	SERVERBOUND_FORWARD
)

const (
	CLIENTBOUND_OK = iota
	CLIENTBOUND_NEVER_SAID_HELLO
	CLIENTBOUND_ALREADY_SAID_HELLO
	CLIENTBOUND_BUSY
	CLIENTBOUND_NOT_AUTHENTICATED
	CLIENTBOUND_TOO_MANY_ATTEMPTS
	CLIENTBOUND_INCORRECT_TOKEN
	CLIENTBOUND_ALREADY_AUTHENTICATED
	CLIENTBOUND_FORWARD
)

const TOKEN_ALPHABET = "0123456789"

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

var busy bool
var tokenAttempts int
var exploitCommunicator communicator.Communicator

func generateToken() string {
	token := ""

	for i := 0; i < 6; i++ {
		generator, err := rand.Int(rand.Reader, big.NewInt(int64(len(TOKEN_ALPHABET))))
		if err != nil {
			panic(err)
		}
		token += string(TOKEN_ALPHABET[generator.Int64()])
	}

	return token
}
func Ws(c *gin.Context) {
	ws, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		panic(err)
	}

	defer ws.Close()

	var saidHello bool
	var authenticated bool

	var token string
	var tokenDisplay *exec.Cmd

	for {
		var packet struct {
			Id      byte   `json:"id"`
			Token   string `json:"token"`
			Forward string `json:"forward"`
		}

		if err = ws.ReadJSON(&packet); err != nil {
			if saidHello {
				busy = false
				tokenDisplay.Process.Kill()
			}
			if authenticated {
				exploitCommunicator.Close()
			}
			panic(err)
		}

		response := gin.H{"id": CLIENTBOUND_OK}

		if !saidHello {
			if packet.Id == SERVERBOUND_HELLO {
				if busy {
					response = gin.H{"id": CLIENTBOUND_BUSY}
				} else {
					token = generateToken()
					tokenDisplay = exec.Command("./token_display/token_display.py", token)
					tokenDisplay.Start()
					saidHello = true
					busy = true
				}
			} else {
				response = gin.H{"id": CLIENTBOUND_NEVER_SAID_HELLO}
			}
		} else if packet.Id == SERVERBOUND_HELLO {
			response = gin.H{"id": CLIENTBOUND_ALREADY_SAID_HELLO}
		} else {
			if authenticated {
				if packet.Id == SERVERBOUND_AUTHENTICATE {
					response = gin.H{"id": CLIENTBOUND_ALREADY_AUTHENTICATED}
				} else {
					response = gin.H{
						"id":      CLIENTBOUND_FORWARD,
						"forward": exploitCommunicator.SendPacket(packet.Forward),
					}
				}
			} else if packet.Id == SERVERBOUND_AUTHENTICATE {
				if packet.Token == token {
					tokenDisplay.Process.Kill()
					exploitCommunicator = communicator.NewCommunicator()
					authenticated = true
				} else if tokenAttempts++; tokenAttempts > 5 {
					response = gin.H{"id": CLIENTBOUND_TOO_MANY_ATTEMPTS}
				} else {
					response = gin.H{"id": CLIENTBOUND_INCORRECT_TOKEN}
				}
			} else {
				response = gin.H{"id": CLIENTBOUND_NOT_AUTHENTICATED}
			}
		}

		if err = ws.WriteJSON(response); err != nil {
			if saidHello {
				busy = false
				tokenDisplay.Process.Kill()
			}
			if authenticated {
				exploitCommunicator.Close()
			}
			panic(err)
		}

		if tokenAttempts > 5 {
			os.Exit(1)
		}
	}
}
