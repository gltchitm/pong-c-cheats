class ClientboundEncryptionRequestPacket:
    PACKET_ID = 0

    def __init__(self, client, public_key, encryption_test_value):
        client.write_uint_16(ClientboundEncryptionRequestPacket.PACKET_ID)
        client.write_bytes(public_key)
        client.write_bytes(encryption_test_value)

        client.flush()
class ClientboundEnableEncryptionPacket:
    PACKET_ID = 1

    def __init__(self, client):
        client.write_uint_16(ClientboundEnableEncryptionPacket.PACKET_ID)

        client.flush()
class ClientboundAuthenticateNowPacket:
    PACKET_ID = 2

    def __init__(self, client):
        client.write_uint_16(ClientboundAuthenticateNowPacket.PACKET_ID)

        client.flush()
class ClientboundAuthenticatedPacket:
    PACKET_ID = 3

    def __init__(self, client):
        client.write_uint_16(ClientboundAuthenticatedPacket.PACKET_ID)

        client.flush()
class ClientboundConnectResponsePacket:
    PACKET_ID = 4

    def __init__(self, client, ok, error=None):
        client.write_uint_16(ClientboundConnectResponsePacket.PACKET_ID)
        client.write_bool(ok)
        if not ok:
            client.write_string(error)

        client.flush()
class ClientboundAttachResponsePacket:
    PACKET_ID = 5

    def __init__(self, client, ok, error=None):
        client.write_uint_16(ClientboundAttachResponsePacket.PACKET_ID)
        client.write_bool(ok)
        if not ok:
            client.write_string(error)

        client.flush()
class ClientboundGetLeftScoreResponsePacket:
    PACKET_ID = 6

    def __init__(self, client, ok, score=None, error=None):
        client.write_uint_16(ClientboundGetLeftScoreResponsePacket.PACKET_ID)
        client.write_bool(ok)
        if not ok:
            client.write_string(error)
        else:
            client.write_uint_32(score)
        client.flush()
class ClientboundGetRightScoreResponsePacket:
    PACKET_ID = 7

    def __init__(self, client, ok, score=None, error=None):
        client.write_uint_16(ClientboundGetRightScoreResponsePacket.PACKET_ID)
        client.write_bool(ok)
        if not ok:
            client.write_string(error)
        else:
            client.write_uint_32(score)
        client.flush()
class ClientboundChangeLeftScoreResponsePacket:
    PACKET_ID = 8

    def __init__(self, client, ok, error=None):
        client.write_uint_16(ClientboundChangeLeftScoreResponsePacket.PACKET_ID)
        client.write_bool(ok)
        if not ok:
            client.write_string(error)

        client.flush()
class ClientboundChangeRightScoreResponsePacket:
    PACKET_ID = 9

    def __init__(self, client, ok, error=None):
        client.write_uint_16(ClientboundChangeRightScoreResponsePacket.PACKET_ID)
        client.write_bool(ok)
        if not ok:
            client.write_string(error)

        client.flush()
class ClientboundDetachResponsePacket:
    PACKET_ID = 10

    def __init__(self, client):
        client.write_uint_16(ClientboundDetachResponsePacket.PACKET_ID)

        client.flush()
class ClientboundKickPacket:
    PACKET_ID = 65535

    def __init__(self, client, reason):
        client.write_uint_16(ClientboundKickPacket.PACKET_ID)
        client.write_string(reason)

        client.flush()
