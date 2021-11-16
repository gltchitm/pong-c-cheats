package main

import (
	"log"
	"runtime"

	"github.com/gin-gonic/gin"
	"github.com/gltchitm/pong-c-cheats/web/controllers/ws"
)

func main() {
	if runtime.GOOS != "linux" {
		panic("Only Linux is supported!")
	}

	router := gin.Default()

	log.SetFlags(log.LstdFlags | log.Lshortfile)

	router.GET("/ws", ws.Ws)

	router.Static("/build", "client/public/build")
	router.StaticFile("/", "client/public/index.html")

	router.Run("localhost:13948")
}
