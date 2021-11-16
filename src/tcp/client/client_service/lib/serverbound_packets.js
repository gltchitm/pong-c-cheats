const { BufferWriter, EncryptedBufferWriter } = require('./buffer_writer')

class ServerboundHelloPacket {
    static PACKET_ID = 0

    constructor(client) {
        const writer = new BufferWriter()
        writer.writeUInt16(ServerboundHelloPacket.PACKET_ID)

        client.write(writer.finish())
    }
}
class ServerboundEncryptionResponsePacket {
    static PACKET_ID = 1

    constructor(client, encryptedEncryptionKey, encryptedEncryptionTestValue) {
        const writer = new BufferWriter()
        writer.writeUInt16(ServerboundEncryptionResponsePacket.PACKET_ID)
        writer.writeBuffer(encryptedEncryptionKey)
        writer.writeBuffer(encryptedEncryptionTestValue)

        client.write(writer.finish())
    }
}
class ServerboundAuthenticatePacket {
    static PACKET_ID = 2

    constructor(client, encryptionKey, token) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundAuthenticatePacket.PACKET_ID)
        writer.writeString(token)

        client.write(writer.finish())
    }
}
class ServerboundConnectPacket {
    static PACKET_ID = 3

    constructor(client, encryptionKey) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundConnectPacket.PACKET_ID)

        client.write(writer.finish())
    }
}
class ServerboundAttachPacket {
    static PACKET_ID = 4

    constructor(client, encryptionKey) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundAttachPacket.PACKET_ID)

        client.write(writer.finish())
    }
}
class ServerboundGetLeftScorePacket {
    static PACKET_ID = 5

    constructor(client, encryptionKey) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundGetLeftScorePacket.PACKET_ID)

        client.write(writer.finish())
    }
}
class ServerboundGetRightScorePacket {
    static PACKET_ID = 6

    constructor(client, encryptionKey) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundGetRightScorePacket.PACKET_ID)

        client.write(writer.finish())
    }
}
class ServerboundChangeLeftScorePacket {
    static PACKET_ID = 7

    constructor(client, encryptionKey, score) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundChangeLeftScorePacket.PACKET_ID)
        writer.writeUInt32(score)

        client.write(writer.finish())
    }
}
class ServerboundChangeRightScorePacket {
    static PACKET_ID = 8

    constructor(client, encryptionKey, score) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundChangeRightScorePacket.PACKET_ID)
        writer.writeUInt32(score)

        client.write(writer.finish())
    }
}
class ServerboundDetachPacket {
    static PACKET_ID = 9

    constructor(client, encryptionKey) {
        const writer = new EncryptedBufferWriter(encryptionKey)
        writer.writeUInt16(ServerboundDetachPacket.PACKET_ID)

        client.write(writer.finish())
    }
}


module.exports = {
    ServerboundHelloPacket,
    ServerboundEncryptionResponsePacket,
    ServerboundAuthenticatePacket,
    ServerboundConnectPacket,
    ServerboundAttachPacket,
    ServerboundGetLeftScorePacket,
    ServerboundGetRightScorePacket,
    ServerboundChangeLeftScorePacket,
    ServerboundChangeRightScorePacket,
    ServerboundDetachPacket
}
