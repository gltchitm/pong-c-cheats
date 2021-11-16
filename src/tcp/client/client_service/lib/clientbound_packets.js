const { createPublicKey } = require('crypto')

class ClientboundEncryptionRequestPacket {
    static PACKET_ID = 0

    constructor(publicKey, encryptionTestValue) {
        this.publicKey = publicKey
        this.encryptionTestValue = encryptionTestValue
    }

    static async read(reader) {
        return new ClientboundEncryptionRequestPacket(
            createPublicKey({
                key: await reader.readBuffer(),
                format: 'der',
                type: 'spki'
            }),
            await reader.readBuffer()
        )
    }
}
class ClientboundEnableEncryptionPacket {
    static PACKET_ID = 1

    static async read() { return new ClientboundEnableEncryptionPacket() }
}
class ClientboundAuthenticateNowPacket {
    static PACKET_ID = 2

    static async read() { return new ClientboundAuthenticateNowPacket() }
}
class ClientboundAuthenticatedPacket {
    static PACKET_ID = 3

    static async read() { return new ClientboundAuthenticatedPacket() }
}
class ClientboundConnectResponsePacket {
    static PACKET_ID = 4

    constructor(error) {
        this.error = error
    }

    static async read(reader) {
        const ok = await reader.readBool()
        let error
        if (!ok) {
            error = await reader.readString()
        }
        return new ClientboundConnectResponsePacket(error)
    }
}
class ClientboundAttachResponsePacket {
    static PACKET_ID = 5

    constructor(error) {
        this.error = error
    }

    static async read(reader) {
        const ok = await reader.readBool()
        let error
        if (!ok) {
            error = await reader.readString()
        }
        return new ClientboundAttachResponsePacket(error)
    }
}
class ClientboundGetLeftScoreResponsePacket {
    static PACKET_ID = 6

    constructor(error, score) {
        this.error = error
        this.score = score
    }

    static async read(reader) {
        const ok = await reader.readBool()
        let error
        let score
        if (!ok) {
            error = await reader.readString()
        } else {
            score = await reader.readUInt32()
        }
        return new ClientboundGetLeftScoreResponsePacket(error, score)
    }
}
class ClientboundGetRightScoreResponsePacket {
    static PACKET_ID = 7

    constructor(error, score) {
        this.error = error
        this.score = score
    }

    static async read(reader) {
        const ok = await reader.readBool()
        let error
        let score
        if (!ok) {
            error = await reader.readString()
        } else {
            score = await reader.readUInt32()
        }
        return new ClientboundGetRightScoreResponsePacket(error, score)
    }
}
class ClientboundChangeLeftScoreResponsePacket {
    static PACKET_ID = 8

    constructor(error) {
        this.error = error
    }

    static async read(reader) {
        const ok = await reader.readBool()
        let error
        if (!ok) {
            error = await reader.readString()
        }
        return new ClientboundChangeLeftScoreResponsePacket(error)
    }
}
class ClientboundChangeRightScoreResponsePacket {
    static PACKET_ID = 9

    constructor(error) {
        this.error = error
    }

    static async read(reader) {
        const ok = await reader.readBool()
        let error
        if (!ok) {
            error = await reader.readString()
        }
        return new ClientboundChangeRightScoreResponsePacket(error)
    }
}
class ClientboundDetachResponsePacket {
    static PACKET_ID = 10

    static async read() { return new ClientboundDetachResponsePacket() }
}

class ClientboundKickPacket {
    static PACKET_ID = 65535

    constructor(reason) {
        this.reason = reason
    }

    static async read(reader) {
        return new ClientboundKickPacket(await reader.readString())
    }
}

module.exports = {
    ClientboundEncryptionRequestPacket,
    ClientboundEnableEncryptionPacket,
    ClientboundAuthenticateNowPacket,
    ClientboundAuthenticatedPacket,
    ClientboundConnectResponsePacket,
    ClientboundAttachResponsePacket,
    ClientboundGetLeftScoreResponsePacket,
    ClientboundGetRightScoreResponsePacket,
    ClientboundChangeLeftScoreResponsePacket,
    ClientboundChangeRightScoreResponsePacket,
    ClientboundDetachResponsePacket,
    ClientboundKickPacket
}
