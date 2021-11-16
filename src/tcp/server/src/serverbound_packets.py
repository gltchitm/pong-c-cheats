class ServerboundHelloPacket:
    PACKET_ID = 0

    def __init__(self, _):
        pass
class ServerboundEncryptionResponsePacket:
    PACKET_ID = 1

    def __init__(self, client):
        self.encrypted_encryption_key = client.read_bytes()
        self.encrypted_encryption_test_value = client.read_bytes()
class ServerboundAuthenticatePacket:
    PACKET_ID = 2

    def __init__(self, client):
        self.token = client.read_string()
class ServerboundConnectPacket:
    PACKET_ID = 3

    def __init__(self, _):
        pass
class ServerboundAttachPacket:
    PACKET_ID = 4

    def __init__(self, _):
        pass
class ServerboundGetLeftScorePacket:
    PACKET_ID = 5

    def __init__(self, _):
        pass
class ServerboundGetRightScorePacket:
    PACKET_ID = 6

    def __init__(self, _):
        pass
class ServerboundChangeLeftScorePacket:
    PACKET_ID = 7

    def __init__(self, client):
        self.score = client.read_uint_32()
class ServerboundChangeRightScorePacket:
    PACKET_ID = 8

    def __init__(self, client):
        self.score = client.read_uint_32()
class ServerboundDetachPacket:
    PACKET_ID = 9

    def __init__(self, _):
        pass
