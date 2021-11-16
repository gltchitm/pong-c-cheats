from socket import socket, AF_INET, SOCK_STREAM, SOL_SOCKET, SO_REUSEADDR
from threading import Thread

from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
from Crypto.Random import get_random_bytes

from auto_client import AutoClient
from auth_server import redeem_token
from crypto import xchacha20_poly1305_decrypt

from serverbound_packets import *
from clientbound_packets import *
from daemon import DaemonSession, safe_disconnect

server = socket(AF_INET, SOCK_STREAM)

server.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
server.bind(('127.0.0.1', 13950))
server.listen(1)

private_key = RSA.generate(1024)
public_key_der = private_key.public_key().export_key('DER')

thread = None
daemon_session = None

def client_handler(client):
    client.read_packet(ServerboundHelloPacket)

    encryption_test_value = get_random_bytes(16)

    ClientboundEncryptionRequestPacket(client, public_key_der, encryption_test_value)

    response = client.read_packet(ServerboundEncryptionResponsePacket)

    rsa_cipher = PKCS1_OAEP.new(private_key)

    encryption_key = rsa_cipher.decrypt(response.encrypted_encryption_key)

    if xchacha20_poly1305_decrypt(
        encryption_key,
        response.encrypted_encryption_test_value
    ) != encryption_test_value:
        client.kick('Encryption test value does not match!')

    ClientboundEnableEncryptionPacket(client)

    client.encryption_key = encryption_key

    ClientboundAuthenticateNowPacket(client)

    response = client.read_packet(ServerboundAuthenticatePacket)

    if not redeem_token(response.token):
        client.kick('Invalid token!')

    ClientboundAuthenticatedPacket(client)

    global daemon_session
    while True:
        packet = client.read_packet((
            ServerboundConnectPacket,
            ServerboundAttachPacket,
            ServerboundGetLeftScorePacket,
            ServerboundGetRightScorePacket,
            ServerboundChangeLeftScorePacket,
            ServerboundChangeRightScorePacket,
            ServerboundDetachPacket
        ))

        if isinstance(packet, ServerboundConnectPacket):
            try:
                daemon_session = DaemonSession()
                ClientboundConnectResponsePacket(client, True)
            except Exception as e:
                safe_disconnect(daemon_session)
                ClientboundConnectResponsePacket(client, False, str(e))
        elif isinstance(packet, ServerboundAttachPacket):
            try:
                daemon_session.attach()
                ClientboundAttachResponsePacket(client, True)
            except Exception as e:
                safe_disconnect(daemon_session)
                ClientboundAttachResponsePacket(client, False, str(e))
        elif isinstance(packet, ServerboundGetLeftScorePacket):
            try:
                ClientboundGetLeftScoreResponsePacket(
                    client,
                    True,
                    daemon_session.get_left_score()
                )
            except Exception as e:
                safe_disconnect(daemon_session)
                ClientboundGetLeftScoreResponsePacket(client, False, error=str(e))
        elif isinstance(packet, ServerboundGetRightScorePacket):
            try:
                ClientboundGetRightScoreResponsePacket(
                    client,
                    True,
                    daemon_session.get_right_score()
                )
            except Exception as e:
                safe_disconnect(daemon_session)
                ClientboundGetRightScoreResponsePacket(client, False, error=str(e))
        elif isinstance(packet, ServerboundChangeLeftScorePacket):
            try:
                daemon_session.change_left_score(packet.score)
                ClientboundChangeLeftScoreResponsePacket(client, True)
            except Exception as e:
                safe_disconnect(daemon_session)
                ClientboundChangeLeftScoreResponsePacket(client, False, error=str(e))
        elif isinstance(packet, ServerboundChangeRightScorePacket):
            try:
                daemon_session.change_right_score(packet.score)
                ClientboundChangeRightScoreResponsePacket(client, True)
            except Exception as e:
                safe_disconnect(daemon_session)
                ClientboundChangeRightScoreResponsePacket(client, False, error=str(e))
        elif isinstance(packet, ServerboundDetachPacket):
            daemon_session.detach()
            safe_disconnect(daemon_session)
            ClientboundDetachResponsePacket(client)

while True:
    client, _ = server.accept()

    if thread and thread.is_alive():
        AutoClient(client).kick('This server is busy.', exit_when_done=False)
    else:
        thread = Thread(target=client_handler, args=(AutoClient(client),))
        thread.start()
