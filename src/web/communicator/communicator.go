package communicator

import (
	"bufio"
	"net"
)

type Communicator struct {
	connection net.Conn
	reader     bufio.Reader
}

func NewCommunicator() Communicator {
	connection, err := net.Dial("unix", "/tmp/pongccheatsd.sock")
	if err != nil {
		panic(err)
	}

	return Communicator{
		connection: connection,
		reader:     *bufio.NewReader(connection),
	}
}

func (communicator Communicator) Close() {
	communicator.connection.Close()
}
func (communicator Communicator) SendPacket(packet string) string {
	communicator.connection.Write([]byte(packet))

	return communicator.readPacket()
}
func (communicator Communicator) readPacket() string {
	packet, err := communicator.reader.ReadString('\n')
	if err != nil {
		panic(err)
	}

	return packet
}
