from enum import IntEnum
from json import loads, dumps
from socket import socket, AF_UNIX, SOCK_STREAM, SHUT_RDWR

class IntEnumExt(IntEnum):
    def __eq__(self, other):
        return int(self) == other
    def __hash__(self):
        return int(self)
class DaemonServerboundPackets(IntEnumExt):
    ATTACH = 0
    CHANGE_LEFT_SCORE = 1
    CHANGE_RIGHT_SCORE = 2
    GET_LEFT_SCORE = 3
    GET_RIGHT_SCORE = 4
    DETACH = 5
    DETACH_AND_EXIT = 6
class DaemonClientboundPackets(IntEnumExt):
    OK = 0
    LEFT_SCORE = 1
    RIGHT_SCORE = 2
    BUSY = 3
    ALREADY_ATTACHED = 4
    NOT_ATTACHED = 5
    NOT_FOUND = 6
    MALFORMED_PACKET = 7

daemon_clientbound_error_descrptions = {
    DaemonClientboundPackets.BUSY: 'The daemon is busy.',
    DaemonClientboundPackets.ALREADY_ATTACHED: 'Already attached!',
    DaemonClientboundPackets.NOT_ATTACHED: 'Not attached!',
    DaemonClientboundPackets.NOT_FOUND: 'Cannot find Pong C!',
    DaemonClientboundPackets.MALFORMED_PACKET: 'Malformed packet!'
}

class DaemonSession:
    def __init__(self):
        self.socket = socket(AF_UNIX, SOCK_STREAM)
        self.socket.connect('/tmp/pongccheatsd.sock')
    def recv(self):
        pass
    def packet(self, packet_id, score=None):
        try:
            self.socket.sendall(
                bytes(
                    dumps(
                        {
                            'id': packet_id,
                            'score': score
                        }
                    ) + '\n',
                    'utf-8'
                )
            )
        finally:
            received = b''

            while not received.endswith(b'\n'):
                byte = self.socket.recv(1)

                if byte == b'':
                    raise Exception('daemon socket closed')

                received += byte

            packet = loads(received)

            error = daemon_clientbound_error_descrptions.get(packet.get('error'))
            if error is not None:
                raise Exception(error)

            if packet['error'] == DaemonClientboundPackets.OK:
                return
            elif packet['error'] in (
                DaemonClientboundPackets.LEFT_SCORE,
                DaemonClientboundPackets.RIGHT_SCORE
            ):
                return packet['score']
            else:
                raise Exception('unexpected packet from daemon')
    def attach(self):
        self.packet(DaemonServerboundPackets.ATTACH)
    def change_left_score(self, score):
        self.packet(DaemonServerboundPackets.CHANGE_LEFT_SCORE, score)
    def change_right_score(self, score):
        self.packet(DaemonServerboundPackets.CHANGE_RIGHT_SCORE, score)
    def get_left_score(self):
        return self.packet(DaemonServerboundPackets.GET_LEFT_SCORE)
    def get_right_score(self):
        return self.packet(DaemonServerboundPackets.GET_RIGHT_SCORE)
    def detach(self):
        self.packet(DaemonServerboundPackets.DETACH)
    def disconnect(self):
        self.socket.shutdown(SHUT_RDWR)
        self.socket.close()

def safe_disconnect(session):
    try:
        session.disconnect()
    except Exception:
        pass
