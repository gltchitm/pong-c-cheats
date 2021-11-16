import struct

from crypto import xchacha20_poly1305_decrypt, xchacha20_poly1305_encrypt
from clientbound_packets import ClientboundKickPacket
from sizeof import SIZEOF_INT_16, SIZEOF_INT_32

from inspect import isclass

class AutoClient:
    def __init__(self, client):
        self.client = client
        self.bytes = b''
        self.encryption_key = None
    def encrypted(self):
        if self.recv == self.__recv_encrypted:
            return self

        encrypted = self.read_bytes()

        decrypted = xchacha20_poly1305_decrypt(self.encryption_key, encrypted)

        client = AutoClient(self.client)

        client.bytes = self.bytes
        client.encryption_key = self.encryption_key
        client.decrypted_bytes = decrypted
        client.recv = client.__recv_encrypted

        return client
    def __recv_encrypted(self, size):
        sliced = self.decrypted_bytes[:size]

        self.decrypted_bytes = self.decrypted_bytes[size:]

        return sliced
    def recv(self, size):
        received = self.client.recv(size)

        while len(received) < size:
            if received == b'':
                raise Exception('Connection closed')

            received = self.client.recv(size - len(received))

        return received
    def read_int(self, size, signed):
        return int.from_bytes(self.recv(size), byteorder='big', signed=signed)
    def read_uint_32(self):
        return self.read_int(SIZEOF_INT_32, False)
    def read_uint_16(self):
        return self.read_int(SIZEOF_INT_16, False)
    def read_string(self):
        return self.read_bytes().decode('utf-8')
    def read_bytes(self):
        return self.recv(self.read_uint_16())
    def read_packet(self, packet_classes):
        if isclass(packet_classes):
            packet_classes = (packet_classes,)

        client = self.encrypted() if self.encryption_key else self

        packet_id = client.read_uint_16()

        for packet_class in packet_classes:
            if packet_id == packet_class.PACKET_ID:
                return packet_class(client)

        self.kick('Unexpected packet!')
    def kick(self, reason, exit_when_done=True):
        ClientboundKickPacket(self, reason)

        self.client.close()

        if exit_when_done:
            exit(1)
    def write_uint_16(self, data):
        self.bytes += struct.pack('>H', data)
    def write_uint_32(self, data):
        self.bytes += struct.pack('>I', data)
    def write_bool(self, data):
        self.bytes += struct.pack('>?', data)
    def write_string(self, data):
        self.write_bytes(bytes(data, 'utf-8'))
    def write_bytes(self, data):
        self.write_uint_16(len(data))
        self.bytes += data
    def flush(self):
        if self.encryption_key:
            encrypted = xchacha20_poly1305_encrypt(self.encryption_key, self.bytes)

            self.bytes = b''
            self.write_bytes(encrypted)

        self.client.sendall(self.bytes)
        self.bytes = b''
